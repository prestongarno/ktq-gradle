package com.prestongarno.ktq.compiler

import com.prestongarno.ktq.compiler.qlang.spec.QCustomScalarType
import org.junit.Test
import kotlin.test.assertTrue
import com.google.common.truth.Truth.assertThat

const val PACK : String = "com.test"

class CustomScalarDefinitions {
  @Test fun testCustomScalarIfaceToKotlin() {
    assertThat(QCustomScalarType.CUSTOM_SCALAR_SUPERIFACE.toKotlin().toString())
        .isEqualTo("interface ${com.prestongarno.ktq.CustomScalar::class.simpleName}" +
            " : com.prestongarno.ktq.QSchemaType\n")
  }

  @Test fun testSingleCustomScalarDefinition() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |scalar URL
          |
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |object URL : CustomScalar
                |
                """.trimMargin("|"))
        }
  }

  @Test fun testSingleFieldOfCustomScalarType() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |scalar URL
          |
          |type Foo {
          |  url: URL
          |}
          |
          """.trimMargin("|"))
        .compile()
        .result {
          println(it)
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |object URL : CustomScalar
                |
                |object Foo : QSchemaType {
                |
                |  val url: CustomScalarInitStub<URL> by QCustomScalar.stub()
                |}
                """.trimMargin("|"))
        }  }
}
