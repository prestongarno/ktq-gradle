package com.prestongarno.ktq.compiler.qlang.spec

import com.squareup.kotlinpoet.TypeSpec
import java.util.*
import java.util.stream.Collectors

sealed class QScalarType(name: String) : QDefinedType(name) {

  override fun toKotlin(): TypeSpec {
    throw IllegalStateException("no") }

  override fun equals(other: Any?): Boolean {
    return other is QScalarType && name == other.name
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

class QId : QScalarType("ID")
/**
 * Enum class representing primitive types
 */
enum class Scalar(val token: String) {
  INT("Int"),
  FLOAT("Float"),
  BOOL("Boolean"),
  STRING("String"),
  ID("ID"),
  UNKNOWN("");

  companion object matcher {
    private val values: Map<String, Scalar>

    init {
      values = Arrays.stream(enumValues<Scalar>()).collect(Collectors.toMap({ t -> t.token }, { t -> t }))
    }

    fun match(keyword: String): Scalar = values[keyword] ?: UNKNOWN

    fun getType(type: Scalar): QScalarType = when (type) {
      INT -> intType
      FLOAT -> floatType
      BOOL -> boolType
      STRING -> stringType
      ID -> idType
      UNKNOWN -> customType
    }

    private val idType = QId()
    private val intType = QInt()
    private val floatType = QFloat()
    private val boolType = QBool()
    private val stringType = QString()
    private val customType = QCustomScalarType("")
  }
}