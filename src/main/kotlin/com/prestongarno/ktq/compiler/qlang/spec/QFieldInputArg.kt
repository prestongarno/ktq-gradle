package com.prestongarno.ktq.compiler.qlang.spec

import com.squareup.kotlinpoet.FunSpec

class QFieldInputArg(name: String,
                     var type: QDefinedType,
                     var defaultValue: String = "",
                     var isList: Boolean = false,
                     var nullable: Boolean = true)
  : QSchemaType<FunSpec>(name) {

  override fun toKotlin(): FunSpec = throw UnsupportedOperationException()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as QFieldInputArg

    return type == other.type
      && defaultValue == other.defaultValue
      && isList == other.isList
      && nullable == other.nullable
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + defaultValue.hashCode()
    result = 31 * result + isList.hashCode()
    result = 31 * result + nullable.hashCode()
    return result
  }
}