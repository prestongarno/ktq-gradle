package com.prestongarno.ktq.compiler

import com.prestongarno.ktq.ArgBuilder
import com.prestongarno.ktq.CustomScalarArgBuilder
import com.prestongarno.ktq.CustomScalarListArgBuilder
import com.prestongarno.ktq.ListArgBuilder
import com.prestongarno.ktq.TypeArgBuilder
import com.prestongarno.ktq.TypeListArgBuilder
import com.prestongarno.ktq.compiler.qlang.spec.QCustomScalarType
import com.prestongarno.ktq.compiler.qlang.spec.QDefinedType
import com.prestongarno.ktq.compiler.qlang.spec.QEnumDef
import com.prestongarno.ktq.compiler.qlang.spec.QField
import com.prestongarno.ktq.compiler.qlang.spec.QInputType
import com.prestongarno.ktq.compiler.qlang.spec.QInterfaceDef
import com.prestongarno.ktq.compiler.qlang.spec.QScalarType
import com.prestongarno.ktq.compiler.qlang.spec.QSchemaType
import com.prestongarno.ktq.compiler.qlang.spec.QStatefulType
import com.prestongarno.ktq.compiler.qlang.spec.QTypeDef
import com.prestongarno.ktq.compiler.qlang.spec.QUnionTypeDef
import com.prestongarno.ktq.compiler.qlang.spec.Scalar
import com.prestongarno.ktq.compiler.qlang.spec.buildArgBuilder
import com.prestongarno.ktq.compiler.qlang.spec.inputBuilderClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import java.util.Optional

/**
 * Contains instance variables of Lists of different kinds of GraphQL schema declarations, each one with a field
 * list of Strings for which each entry represents a declaration within a type declaration (primitive model, field
 * type, or field list of types
 */
class QCompilationUnit(val all: Collection<QSchemaType<*>>) {

  val enums: List<QEnumDef> = all.filter { it is QEnumDef }.map { it as QEnumDef }
  val types: List<QTypeDef> = filterIs(all)
  val ifaces: List<QInterfaceDef> = filterIs(all)
  val inputs: List<QInputType> = filterIs(all)
  val scalar: List<QScalarType> = filterIs(all)
  val unions: List<QUnionTypeDef> = filterIs(all)

  private inline fun <reified T> filterIs(from: Collection<Any>): List<T> = from.filterIsInstance<T>()


  fun getAllTypes(): List<TypeSpec> =
      resolveConflicts()
          .also { lizt ->
            lizt.addAll(all.filterIsInstance<QStatefulType>().map {
              if (it.description.isNotEmpty())
                it.toKotlin()
                    .toBuilder()
                    .addKdoc(CodeBlock.of(it.description, "%W"))
                    .build()
              else it.toKotlin()
            }.toList()
                .sortedBy { it.name })
          }

  private val conflictOverrides = mutableMapOf<QField, Pair<QTypeDef, List<QInterfaceDef>>>()

  fun addConflict(conflict: Pair<QField, Pair<QTypeDef, List<QInterfaceDef>>>) =
      conflictOverrides.put(conflict.first, conflict.second)

  val stateful: Map<String, QStatefulType> by lazy {
    (scalar + enums + types + ifaces + unions + inputs).map { Pair(it.name, it) }.toMap()
  }

  fun findType(key: String): QDefinedType? = stateful[key] ?: Scalar.getType(Scalar.match(key))

  private fun resolveConflicts(): MutableList<TypeSpec> {
    return this.conflictOverrides.toList().mapNotNull { (symbol, pair: Pair<QTypeDef, List<QInterfaceDef>>) ->
      val baseInputClazzName = inputBuilderClassName(symbol.name)
      val superclazzType: TypeName = ClassName.bestGuess("Base" + baseInputClazzName)
      // 1) check that all multi-inherited fields are:
      //      i ) same type
      //      ii) concrete field declares all required args as the iface
      //      iii)extending #i, but checking `isList`, `nullable`, etc.
      verifyOverridingFields(symbol, pair.first, pair.second)
          .ifPresent { throw it }
      // need to build subclass for fields that have input argument(s)
      if (symbol.args.isNotEmpty()) {
        // 2) Add an argument builder class inside of the type/class for that particular field, extends #3
        // 3) Create top-level abstract builder class which unifies multi-override difference
        val dummy = QField(
            symbol.name,
            symbol.type,
            emptyList(),
            symbol.directive,
            symbol.isList,
            symbol.nullable).also { it.flag(QField.BuilderStatus.TOP_LEVEL); it.abstract(true) }
        buildArgBuilder(dummy,
            if (!dummy.isList) {
              if (dummy.type is QCustomScalarType)
                CustomScalarArgBuilder::class
              else if (dummy.type is QScalarType || dummy.type is QEnumDef)
                ArgBuilder::class
              else
                TypeArgBuilder::class
            } else {
              if (dummy.type is QCustomScalarType)
                CustomScalarListArgBuilder::class
              else if (dummy.type is QScalarType || dummy.type is QEnumDef)
                ListArgBuilder::class
              else
                TypeListArgBuilder::class
            },
            superclazzType)
            .addModifiers(KModifier.ABSTRACT)
            .build()
      } else null
    }.distinctBy { it.name }
        .toMutableList()
  }

  private fun verifyOverridingFields(symbol: QField,
                                     declaring: QTypeDef,
                                     overriding: List<QInterfaceDef>): Optional<Throwable> {
    overriding.fold(overriding[0], { curr, next ->
      val first = curr.fields.find { it.type.name == symbol.type.name }!! // would have failed in attr stage if null
      val verifi = verifyInputArguments(symbol, first)

      if (verifi.isPresent)
        return Optional.of(Throwable("Input argument mismatch on type ${declaring.name}: ", verifi.get()))
      val comparingTo = next.fields.find { it.type.name == symbol.type.name }!!

      if (first.type != comparingTo.type)
        return Optional.of(IllegalStateException("Conflicting overriding property declarations on [ ${declaring.name}.${symbol.name} ] " +
            "from interfaces ${curr.name} ( ${symbol.name} declares type ${first.type.name} )" +
            " and ${next.name} (declares type ${comparingTo.type.name} )"))

      next
    })
    return Optional.empty()
  }

  /**
   * If conflicting input declarations, returns a throwable (in the optional)
   */
  private fun verifyInputArguments(symbol: QField, comparing: QField): Optional<Throwable> {
    comparing.args.forEach { arg ->
      symbol.args.find { it.name == arg.name }?.also {
        if (it.nullable != arg.nullable)
          return Optional.of(Throwable(conflictMessage("nullable", it.nullable, it.name, arg.name)))
        else if (it.isList != arg.isList)
          return Optional.of(Throwable(conflictMessage("list", it.isList, it.name, arg.name)))
        else if (it.type.name != arg.type.name)
          return Optional.of(Throwable(conflictMessage(arg.type.name, false, it.name, arg.name)))
      }
    }
    return Optional.empty()
  }

  private fun conflictMessage(property: String, concreteFieldCondition: Boolean, propName: String, argName: String)
      = "Conflicting input argument declaration, ${boolToStr(concreteFieldCondition, property)} " +
      "$ arg $propName overriding ${boolToStr(!concreteFieldCondition, property)} $propName arg $argName"

  private fun boolToStr(ifIs: Boolean, name: String) = if (ifIs) "" else "non-$name"
}
