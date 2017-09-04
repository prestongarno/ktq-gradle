package com.prestongarno.ktq.compiler.parsing


import com.prestongarno.ktq.compiler.QCompiler
import com.prestongarno.ktq.compiler.TestContext
import com.prestongarno.ktq.compiler.child
import java.io.File
import org.junit.Test

class CustomSchemaTests {


  @Test
  fun testConflictingOverridesPass() {
    val file = this::class.java.classLoader.getResource("sample.schema.graphqls")
    QCompiler.initialize("SampleOne")
        .packageName("com.prestongarno.ktq")
        .compile(File(file.path))
        .writeToFile(TestContext.outputRoot.absolutePath)
  }

  @Test
  fun testYelp() {
    val file = this::class.java.classLoader.getResource("yelp.graphqls")
    val outputRoot = TestContext.outputRoot
    QCompiler.initialize("YelpGraphql")
        .packageName("com.prestongarno.ktq.yelp")
        .compile(File(file.path))
        .result {}
        .writeToFile(TestContext.outputRoot.absolutePath)
  }
}

