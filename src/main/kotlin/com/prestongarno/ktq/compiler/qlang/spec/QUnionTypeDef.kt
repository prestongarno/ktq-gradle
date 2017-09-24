package com.prestongarno.ktq.compiler.qlang.spec

import com.prestongarno.ktq.QSchemaUnion
import com.squareup.kotlinpoet.TypeSpec

class QUnionTypeDef(name: String, var possibleTypes: List<QDefinedType>) : QStatefulType(name, emptyList()) {
  override fun toKotlin(): TypeSpec {
    if (this.kotlinSpec == null) {
      this.kotlinSpec = TypeSpec.objectBuilder(name)
          .addSuperinterface(QSchemaUnion::class)
          .addProperties(possibleTypes.map {
            QField(it.name, it, emptyList(), QDirectiveSymbol.default, true, false).toKotlin().first
          }).build()
    }
    return this.kotlinSpec!!
  }
}