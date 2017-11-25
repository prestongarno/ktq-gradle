package com.prestongarno.ktq.compiler.javac

import com.prestongarno.ktq.compiler.eq
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties

infix fun KClass<*>.directlyImplements(superinterface: KClass<*>) {
  supertypes.find { it.classifier == superinterface }
      ?: throw  IllegalArgumentException(
      "${this.qualifiedName} does not implent ${superinterface.qualifiedName}")
}

infix fun KClass<*>.implements(superinterface: KClass<*>) {
  allSuperclasses.find { it == superinterface }
      ?: throw  IllegalArgumentException(
      "${this.qualifiedName} does not implent ${superinterface.qualifiedName}")
}

fun KClass<*>.onFunction(name: String, block: KFunction<*>.() -> Unit) {
  functions.find { it.name == name }?.apply(block)
      ?: throw IllegalArgumentException("No such function '$name' on class $simpleName")
}

fun KClass<*>.onProperty(name: String, block: KProperty<*>.() -> Unit) {
  memberProperties.find { it.name == name }?.apply(block)
      ?: throw IllegalArgumentException("No such property '$name' on class $simpleName")
}

infix fun KProperty<*>.requireReturns(match: KClass<*>) {
  returnType.classifier eq match
}

infix fun KFunction<*>.requireReturns(match: KClass<*>) {
  returnType.classifier eq match
}

fun KProperty<*>.typeArgumentsMatch(match: List<String>) {
  returnType.arguments.mapNotNull(KTypeProjection::type)
      .mapNotNull(KType::classifier)
      .map { it as KClass<*> }
      .mapNotNull(KClass<*>::simpleName)
      .forEachIndexed { index, name ->
        require(match[index] == name)
      }
}
