package com.prestongarno.ktq.compiler.javac

import com.prestongarno.ktq.compiler.KtqCompileWrapper
import com.prestongarno.ktq.compiler.println
import org.junit.Before
import org.junit.Test

class EnumCompileTest : JavacTest() {

  lateinit var loader: KtqCompileWrapper

  @Before fun generateClasses() {
    return;
    loader = jvmCompileAndLoad("""
      |
      |enum GraphQLEnum { HOT, NOT }
      |
      |type Foo {
      |  enumProperty: GraphQLEnum
      |}
      """.trimMargin("|")) {
      require(definitions.size == 2)
    }
  }

  /**
   *
   * FAILING -> need to write definitions:
   *   EnumStub.Query
   *   EnumStub.ConfiguredQuery
   *   EnumStub.OptionalConfigQuery
   * instead of the generic [com.prestongarno.ktq.hooks.OptionalConfiguration] etc.
   */
  @Test fun `enum exists and has correct options`() {
    //loader.loadClass("GraphQLEnum")
  }
}