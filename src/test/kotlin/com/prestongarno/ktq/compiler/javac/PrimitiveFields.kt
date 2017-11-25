package com.prestongarno.ktq.compiler.javac

import com.prestongarno.ktq.QType
import com.prestongarno.ktq.compiler.eq
import com.prestongarno.ktq.stubs.BooleanDelegate
import com.prestongarno.ktq.stubs.FloatDelegate
import com.prestongarno.ktq.stubs.IntDelegate
import com.prestongarno.ktq.stubs.StringDelegate
import org.junit.Test
import kotlin.reflect.full.functions

class PrimitiveFields : JavacTest() {

  @Test fun `single integer field compiles and returns correct type`() {
    val schemaClass = compileAndLoad("""
      |type Definition {
      |  value: Int
      |}
      """.trimMargin("|"), "com.test")
        .loadObject("com.test.Definition")::class

    schemaClass directlyImplements QType::class
    schemaClass.onProperty("value") {
      name eq "value"
      this requireReturns IntDelegate.Query::class
    }
  }

  @Test fun `string field returns correct type`() {
    val schemaClass = compileAndLoad("""
      |type Def2 {  fieldValue: String
      |}
      |""".trimMargin("|"))
        .loadObject("Def2")::class

    schemaClass directlyImplements QType::class
    schemaClass.onProperty("fieldValue") {
      requireReturns(StringDelegate.Query::class)
    }
  }

  @Test fun `float field returns correct type`() {
    val schemaClass = compileAndLoad("""
      |type Def35{
      |floatfield: Float
      |}
      |""".trimMargin("|"))
        .loadObject("Def35")::class

    schemaClass directlyImplements QType::class
    schemaClass.onProperty("floatfield") {
      requireReturns(FloatDelegate.Query::class)
    }

  }

  @Test fun `boolean field returns correctly`() {
       val schemaClass = compileAndLoad("""
      |type StarWars                                                  {
      |                    boo: Boolean
      |                                   }
      |""".trimMargin("|"))
        .loadObject("StarWars")::class

    schemaClass directlyImplements QType::class
    schemaClass.onProperty("boo") {
      requireReturns(BooleanDelegate.Query::class)
    }
  }
}
