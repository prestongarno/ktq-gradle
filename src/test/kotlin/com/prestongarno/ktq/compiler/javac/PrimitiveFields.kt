package com.prestongarno.ktq.compiler.javac

import com.prestongarno.ktq.QType
import com.prestongarno.ktq.compiler.eq
import com.prestongarno.ktq.stubs.BooleanDelegate
import com.prestongarno.ktq.stubs.FloatDelegate
import com.prestongarno.ktq.stubs.IntDelegate
import com.prestongarno.ktq.stubs.StringDelegate
import org.junit.Test

class PrimitiveFields : JavacTest() {

  @Test fun `single integer field compiles and returns correct type`() {
    val schemaClass = jvmCompileAndLoad("""
      |type Definition {
      |  value: Int
      |}
      """.trimMargin("|"), "com.test")
        .loadObject("com.test.Definition")::class

    schemaClass directlyImplements QType::class
    schemaClass.kprop("value") {
      name eq "value"
      this requireReturns IntDelegate.Query::class
    }
  }

  @Test fun `string field returns correct type`() {
    val schemaClass = jvmCompileAndLoad("""
      |type Def2 {  fieldValue: String
      |}
      |""".trimMargin("|"))
        .loadObject("Def2")::class

    schemaClass directlyImplements QType::class
    schemaClass.kprop("fieldValue") {
      requireReturns(StringDelegate.Query::class)
    }
  }

  @Test fun `float field returns correct type`() {
    val schemaClass = jvmCompileAndLoad("""
      |type Def35{
      |floatfield: Float
      |}
      |""".trimMargin("|"))
        .loadObject("Def35")::class

    schemaClass directlyImplements QType::class
    schemaClass.kprop("floatfield") {
      requireReturns(FloatDelegate.Query::class)
    }

  }

  @Test fun `boolean field returns correctly`() {
       val schemaClass = jvmCompileAndLoad("""
      |type StarWars                                                  {
      |                    boo: Boolean
      |                                   }
      |""".trimMargin("|"))
        .loadObject("StarWars")::class

    schemaClass directlyImplements QType::class
    schemaClass.kprop("boo") {
      requireReturns(BooleanDelegate.Query::class)
    }
  }
}
