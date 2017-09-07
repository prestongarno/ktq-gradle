package com.prestongarno.ktq.compiler.parsing


import com.prestongarno.ktq.compiler.QCompiler
import java.io.File
import org.junit.Test

class CustomSchemaTests {


  @Test
  fun testConflictingOverridesPass() {
    val file = this::class.java.classLoader.getResource("sample.schema.graphqls")
    QCompiler.initialize()
        .packageName("com.prestongarno.ktq")
        .compile(File(file.path))
        .result { require(it.isNotEmpty()) }
  }

  @Test
  fun testYelp() {
    val file = this::class.java.classLoader.getResource("yelp.graphqls")
    QCompiler.initialize()
        .packageName("com.prestongarno.ktq.yelp")
        .compile(File(file.path))
        .result { require(it.isNotEmpty()) }
  }
}

