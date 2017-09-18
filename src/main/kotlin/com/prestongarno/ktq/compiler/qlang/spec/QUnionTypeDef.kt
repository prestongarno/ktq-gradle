package com.prestongarno.ktq.compiler.qlang.spec

import com.squareup.kotlinpoet.TypeSpec

class QUnionTypeDef(name: String, var possibleTypes: List<QDefinedType>) : QStatefulType(name, emptyList()) {
  override fun toKotlin(): TypeSpec {
    if (this.kotlinSpec == null) {
      this.kotlinSpec = QTypeDef(name, emptyList(), possibleTypes.map {
        QField(it.name, it, emptyList(), QDirectiveSymbol.default, true, false)
      }).toKotlin()
    }
    return this.kotlinSpec!!
  }
}