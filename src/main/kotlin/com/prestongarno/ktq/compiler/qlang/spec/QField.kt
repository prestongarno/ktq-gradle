package com.prestongarno.ktq.compiler.qlang.spec

import com.prestongarno.ktq.ArgBuilder
import com.prestongarno.ktq.CustomScalarArgBuilder
import com.prestongarno.ktq.CustomScalarConfigStub
import com.prestongarno.ktq.CustomScalarInitStub
import com.prestongarno.ktq.CustomScalarListArgBuilder
import com.prestongarno.ktq.CustomScalarListConfigStub
import com.prestongarno.ktq.CustomScalarListInitStub
import com.prestongarno.ktq.InitStub
import com.prestongarno.ktq.ListConfig
import com.prestongarno.ktq.ListConfigType
import com.prestongarno.ktq.ListInitStub
import com.prestongarno.ktq.QConfigStub
import com.prestongarno.ktq.QTypeConfigStub
import com.prestongarno.ktq.QSchemaType.QScalar
import com.prestongarno.ktq.QSchemaType.QScalarArray
import com.prestongarno.ktq.QSchemaType.QType
import com.prestongarno.ktq.QSchemaType.QCustomScalar
import com.prestongarno.ktq.QSchemaType.QCustomScalarList
import com.prestongarno.ktq.QSchemaType.QTypeList
import com.prestongarno.ktq.adapters.BooleanArrayDelegate
import com.prestongarno.ktq.adapters.BooleanDelegate
import com.prestongarno.ktq.adapters.FloatArrayDelegate
import com.prestongarno.ktq.adapters.FloatDelegate
import com.prestongarno.ktq.adapters.IntegerArrayDelegate
import com.prestongarno.ktq.adapters.IntegerDelegate
import com.prestongarno.ktq.adapters.StringArrayDelegate
import com.prestongarno.ktq.adapters.StringDelegate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import java.util.Optional
import kotlin.reflect.KClass

class QField(name: String,
             var type: QDefinedType,
             var args: List<QFieldInputArg>,
             var directive: QDirectiveSymbol = QDirectiveSymbol.default,
             var isList: Boolean = false,
             var nullable: Boolean = false,
             comment: String = "")
  : QSchemaType<Pair<PropertySpec, Optional<TypeSpec>>>(name) {

  init {
    super.description = comment
    nullable = !isList
        && type !is QScalarType
        && nullable
  }

  enum class BuilderStatus {
    NONE,
    ENCLOSING,
    TOP_LEVEL
  }

  var builderStatus: BuilderStatus
  var abstract: Boolean = false

  init {
    builderStatus = if (args.isEmpty()) BuilderStatus.NONE else BuilderStatus.ENCLOSING
  }

  fun abstract(abstract: Boolean) = apply { this.abstract = abstract }
  fun flag(status: BuilderStatus) = apply { this.builderStatus = status }

  var inheritedFrom = mutableListOf<QInterfaceDef>()

  override fun toKotlin(): Pair<PropertySpec, Optional<TypeSpec>> {
    if (this.kotlinSpec != null) return kotlinSpec!!

    val typeName = if (type !is QScalarType) determineTypeName(this)
        else ClassName.bestGuess("ArgBuilder")
    val rawTypeName =
        if (this.args.isEmpty()) {
          val stubType: KClass<*> =
              if (!isList) {
                when {
                  type is QCustomScalarType -> CustomScalarInitStub::class
                  this.type is QScalarType -> primitiveTypeNameToStubClass(this.type.name, isList = false)
                  this.type is QEnumDef -> throw UnsupportedOperationException()
                  else -> InitStub::class
                }
              } else {
                when {
                  type is QCustomScalarType -> CustomScalarListInitStub::class
                  this.type is QScalarType -> primitiveTypeNameToStubClass(this.type.name, isList = true)
                  this.type is QEnumDef -> throw UnsupportedOperationException()
                  else -> ListInitStub::class
                }
              }

          ParameterizedTypeName.get(ClassName.bestGuess("${stubType.simpleName}"), typeName)

        } else {
          val configName =
              if (!isList) {
                if (type is QCustomScalarType)
                  CustomScalarConfigStub::class.simpleName
                else if (this.type is QScalarType || this.type is QEnumDef)
                  QConfigStub::class.simpleName
                else
                  QTypeConfigStub::class.simpleName
              } else {
                if (type is QCustomScalarType)
                  CustomScalarListConfigStub::class.simpleName
                else if (this.type is QScalarType || this.type is QEnumDef)
                  ListConfig::class.simpleName
                else ListConfigType::class.simpleName
              }
          ParameterizedTypeName.get(ClassName.bestGuess("$configName"), typeName,
              if (abstract) {
                // TOP_LEVEL on abstract means that it's a Base... name, enclosing means on iface,
                when (builderStatus) {
                  BuilderStatus.NONE -> throw IllegalStateException()
                  BuilderStatus.ENCLOSING -> ClassName.bestGuess(inputBuilderClassName(name))
                  BuilderStatus.TOP_LEVEL -> ClassName.bestGuess("Base" + inputBuilderClassName(name))
                }
              } else {
                // concrete -> needs to declare type if TOP_LEVEL as enclosing (inherited from Base_)
                when (builderStatus) {
                  BuilderStatus.NONE -> throw IllegalStateException()
                  BuilderStatus.ENCLOSING ->
                    if (inheritedFrom.isEmpty() || inheritedFrom[0].fieldMap[name]!!.builderStatus == BuilderStatus.TOP_LEVEL)
                      ClassName.bestGuess(inputBuilderClassName(name))
                    else
                      ClassName.bestGuess(inheritedFrom[0].name).nestedClass(inputBuilderClassName(name))
                  BuilderStatus.TOP_LEVEL -> if (inheritedFrom.isEmpty()) throw IllegalStateException()
                  else inputBuilderClassName(name).let { ClassName.bestGuess(it) }
                }
              }
          )
        }
    val complex = determineArgBuilderType(this)
    val argBuilderSpec = // This can be cleaned up
        if (this.args.isEmpty()) {
          Optional.empty()
        } else if (this.inheritedFrom.isEmpty() || abstract && builderStatus != BuilderStatus.TOP_LEVEL) {
          Optional.of(buildArgBuilder(this, complex, ClassName.bestGuess(inputBuilderClassName(this.name))).build())
        } else {
          (checkSuper())
        }

    val spec = PropertySpec.builder(this.name, rawTypeName)
    if (description.isNotEmpty())
      spec.addKdoc(CodeBlock.of(description, "%W"))
    if (!abstract) {
      spec.delegate(
          if (args.isEmpty()) {
            initializerNoConfig()
          } else {
            initializerConfig(
                if (argBuilderSpec.isPresent)
                  ClassName.bestGuess(argBuilderSpec.get().name ?: throw IllegalStateException("report this"))
                else
                  ClassName.bestGuess(inheritedFrom[0].name).nestedClass(inputBuilderClassName(this.name)))
          })
    }
    if (inheritedFrom.isNotEmpty())
      spec.addModifiers(KModifier.OVERRIDE)
    this.kotlinSpec = Pair(spec.build(), argBuilderSpec)
    return this.kotlinSpec!!
  }

  private fun checkSuper(): Optional<TypeSpec> {
    val superField = inheritedFrom[0].fieldMap[name] ?: throw IllegalStateException()
    require(this.builderStatus == superField.builderStatus)
    return if (superField.builderStatus == BuilderStatus.TOP_LEVEL)
      Optional.of(buildArgBuilder(this, ClassName.bestGuess("Base" + inputBuilderClassName(this.name))).build())
    else Optional.empty()
  }

  private fun initializerNoConfig(): CodeBlock {
    if (abstract || args.isNotEmpty()) throw IllegalStateException()
    return CodeBlock.of("${getStubTargetInvoke(this)}()")
  }

  private fun initializerConfig(argTypeName: TypeName): CodeBlock {
    if (abstract || args.isEmpty()) throw IllegalStateException("abstract=$abstract args=$args")

    val constructorInvocation = "${argTypeName.asNonNullable()}(it)"
    if (builderStatus == BuilderStatus.NONE) throw IllegalStateException()
    return CodeBlock.of("${getStubTargetInvoke(this)} { $constructorInvocation }"
    )
  }

  /**
   * NOTE -> only compare the information available
   * from a single scope (i.e. no inheritance)
   */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is QField) return false

    return isList == other.isList
        && nullable == other.nullable
        && type == other.type
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + args.hashCode()
    result = 31 * result + isList.hashCode()
    result = 31 * result + nullable.hashCode()
    return result
  }

}

fun inputBuilderClassName(forField: String): String = "${forField[0].toUpperCase()}${forField.substring(1)}Args"

fun buildArgBuilder(field: QField, superInterface: KClass<*>, name: TypeName): TypeSpec.Builder {
  val result = TypeSpec.classBuilder(name.toString())
      .primaryConstructor(FunSpec.constructorBuilder()
          .addParameter(ParameterSpec.builder("args", superInterface)
              .build())
          .build())
      .addSuperinterface(ClassName.bestGuess(superInterface.simpleName?.replace("$".toRegex(), "_by_args")!!))
  field.args.map {
    builderTypesMethod(determineTypeName(it), it, name.toString())
  }.let { result.addFunctions(it) } // kotlinpoet bug
  return result
}

/**
 * Builds an argbuilder inheriting from a top-level class denoted by `superclass`
 * @param field the field to build the argbuilder for
 * @param superclass the supertype of the argbuilder class
 */
fun buildArgBuilder(field: QField, superclass: TypeName): TypeSpec.Builder {
  val rawType = determineArgBuilderType(field)
  val argClassName = ClassName.bestGuess(inputBuilderClassName(field.name))

  val argBuilderSpec = TypeSpec.classBuilder(argClassName.toString())
      .primaryConstructor(FunSpec.constructorBuilder()
          .addParameter(ParameterSpec.builder("args", rawType)
              .build())
          .build())
      .superclass(superclass)
      .addSuperclassConstructorParameter(CodeBlock.of("args"))

  field.args.map {
    builderTypesMethod(determineTypeName(it), it, argClassName.toString())
  }.let { argBuilderSpec.addFunctions(it) } // kotlinpoet bug
  return argBuilderSpec
}

private fun determineArgBuilderType(field: QField): KClass<*> =
    when {
      field.type is QCustomScalarType -> CustomScalarArgBuilder::class
      field.type is QCustomScalarType -> CustomScalarListArgBuilder::class
      else -> ArgBuilder::class
    }

private fun getStubTargetInvoke(field: QField): String =
    (if (!field.isList) {
      if (field.type is QCustomScalarType)
        "${QCustomScalar::class.simpleName}.stub"
      else if (field.type is QScalarType || field.type is QEnumDef) {
        "${QScalar::class.simpleName}.${field.type.name.toLowerCase()}Stub"
      } else
        "${QType::class.simpleName}.stub"
    } else {
      if (field.type is QCustomScalarType)
        "${QCustomScalarList::class.simpleName}.stub"
      else if (field.type is QScalarType || field.type is QEnumDef) {
        "${QScalarArray::class.simpleName}.${field.type.name.toLowerCase()}ArrayStub"
      } else
        QTypeList::class.simpleName + ".stub"
    })

private fun builderTypesMethod(typeName: TypeName, param: QFieldInputArg, inputClazzName: String) =
    FunSpec.builder(param.name)
        .addParameter("value", typeName)
        .addCode(CodeBlock.builder().addStatement("return apply { addArg(\"${param.name}\", value) }\n").build())
        .returns(ClassName.bestGuess(inputClazzName))
        .build()

private fun primitiveTypeNameToStubClass(name: String, isList: Boolean = false): KClass<*> {
  return if (!isList) {
    when (name) {
      "Int" -> IntegerDelegate::class
      "String" -> StringDelegate::class
      "Float" -> FloatDelegate::class
      "Boolean" -> BooleanDelegate::class
      else -> throw IllegalArgumentException("Unknown type '$name'")
    }
  } else when (name) {
    "Int" -> IntegerArrayDelegate::class
    "String" -> StringArrayDelegate::class
    "Float" -> FloatArrayDelegate::class
    "Boolean" -> BooleanArrayDelegate::class
    else -> throw IllegalArgumentException("Unknown type '$name'")
  }
}
