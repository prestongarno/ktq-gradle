package com.prestongarno.ktq.compiler.qlang.spec

import java.util.*
import java.util.stream.Collectors

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
      ID -> stringType
      UNKNOWN -> customType
    }

    private val intType = QInt();
    private val floatType = QFloat();
    private val boolType = QBool();
    private val stringType = QString();
    private val customType = QCustomScalarType("");
  }
}