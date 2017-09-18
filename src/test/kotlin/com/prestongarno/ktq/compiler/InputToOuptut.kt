package com.prestongarno.ktq.compiler

import org.junit.Test
import com.google.common.truth.Truth.assertThat
import kotlin.text.RegexOption.MULTILINE

class InputToOuptut {
  @Test
  fun testNullableTypesParsedCorrectly() {
    QCompiler.initialize()
        .packageName("com.authentic.mexican")
        .schema("""
        |
        |type Taco {
        |  contents: [Condiment]
        |  price: Float
        |}
        |
        |type Condiment {
        |  name: String!
        |}
        |
    """.trimMargin()).compile()
        .result {
          assertThat(it.minusMetadata()).isEqualTo("""
                |object Condiment : QSchemaType {
                |  val name: Stub<String> by QScalar.stub()
                |}
                |
                |object Taco : QSchemaType {
                |  val contents: ListInitStub<Condiment> by QTypeList.stub()
                |
                |  val price: Stub<Float> by QScalar.stub()
                |}
                |
              """.trimMargin())
        }
  }
}

fun String.minusMetadata() = Regex("^.*import.*\n\n", MULTILINE).split(this).last()