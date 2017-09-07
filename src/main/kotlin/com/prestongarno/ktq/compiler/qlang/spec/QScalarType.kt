package com.prestongarno.ktq.compiler.qlang.spec

import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KClass

sealed class QScalarType(name: String) : QDefinedType(name) {

  override fun toKotlin(): TypeSpec {
    throw IllegalStateException("no") }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return name == (other as QScalarType).name
  }

  override fun hashCode(): Int = javaClass.hashCode()
}

class QCustomScalarType(name: String) : QScalarType(name) {
  override fun toKotlin(): TypeSpec {
    if (this.kotlinSpec == null) this.kotlinSpec = QTypeDef(name,
        emptyList(),
        listOf(QField("value",
            Scalar.getType(Scalar.STRING),
            emptyList(),
            QDirectiveSymbol.default,
            false,
            false))).toKotlin()

    return this.kotlinSpec!!
  }
}

class QInt(val defValue: Int = 0) : QScalarType("Int")

class QFloat(val defValue: Float = 0f) : QScalarType("Float")

class QBool(val defValue: Boolean = false) : QScalarType("Boolean")

class QString(val defValue: String = "") : QScalarType("String")
