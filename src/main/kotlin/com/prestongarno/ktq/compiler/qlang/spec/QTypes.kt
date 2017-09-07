package com.prestongarno.ktq.compiler.qlang.spec

import com.squareup.kotlinpoet.TypeSpec

/** The base class for all components of the compilation
 */
abstract class QSchemaType<E>(var name: String) {

  protected var kotlinSpec: E? = null // sue me

  abstract fun toKotlin() : E

  var description: String = ""

  override fun toString(): String = "'$name' (${this::class.simpleName})"
}

/** * Base class for all "types" defined by the schema
 */
abstract class QDefinedType(name: String) : QSchemaType<TypeSpec>(name)

abstract class QStatefulType(name: String, val fields: List<QField>) : QDefinedType(name) {

  val fieldMap = fields.map { Pair(it.name, it) }.toMap()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as QStatefulType

    return fields.containsAll(other.fields)
  }

  override fun hashCode(): Int {
    var result = fields.hashCode()
    result = 31 * result + fieldMap.hashCode()
    return result
  }
}

class QUnknownType(name: String) : QDefinedType(name) {
  override fun toKotlin(): TypeSpec {
    throw IllegalStateException("This shouldnt happen")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return name == (other as QUnknownType).name
  }

  override fun hashCode(): Int { return javaClass.hashCode() }
}

class QUnknownInterface(name: String) : QInterfaceDef(name, emptyList()) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return name == (other as QUnknownInterface).name
  }

  override fun hashCode(): Int { return javaClass.hashCode() }
}

class QDirectiveSymbol(val type: QDefinedType, val value: String) : QSchemaType<Any>(type.name) {
  override fun toKotlin(): TypeSpec = throw IllegalStateException("This shouldnt happen")
  companion object {
    val default = QDirectiveSymbol(QUnknownType(""), "")
  }
}
