package com.prestongarno.ktq.compiler.parsing


import com.prestongarno.ktq.compiler.QContext
import com.prestongarno.ktq.compiler.QCompiler
import com.prestongarno.ktq.compiler.TestContext
import com.prestongarno.ktq.compiler.child
import org.junit.Before
import java.io.File
import org.junit.Test
import java.net.URI

class CustomSchemaTests {


  @Test
  fun testConflictingOverridesPass() {
    val file = this::class.java.classLoader.getResource("sample.schema.graphqls")
    QCompiler.initialize("SampleOne")
        .packageName("com.prestongarno.ktq")
        .compile(File(file.path))
        .writeToFile(File(TestContext.outputRoot.path + "/SampleOne.kt"))
  }

  @Test
  fun testYelp() {
    val file = this::class.java.classLoader.getResource("yelp.graphqls")
    val outputRoot = TestContext.outputRoot
    QCompiler.initialize("YelpGraphql")
        .packageName("com.prestongarno.ktq.yelp")
        .compile(File(file.path))
        .result {}
        .writeToFile(File(outputRoot).child("YelpGraphql.kt"))
  }
}

