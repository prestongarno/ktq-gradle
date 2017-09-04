package com.prestongarno.ktq.compiler.parsing


import com.prestongarno.ktq.compiler.QCompiler
import com.prestongarno.ktq.compiler.child
import org.junit.Before
import java.io.File
import org.junit.Test
import java.net.URI

class CustomSchemaTests {

  lateinit var mockDir: URI

  @Before
  fun setUp() {
    mockDir = this::class.java
        .classLoader
        .getResource(System.getProperty("-D" + this::class.java.`package`.name))
        .toURI() ?: throw IllegalStateException("Mock project directory not set!")
  }

  @Test
  fun testConflictingOverridesPass() {
    val file = this::class.java.classLoader.getResource("sample.schema.graphqls")
    QCompiler.initialize("SampleOne")
        .packageName("com.prestongarno.ktq")
        .compile(File(file.path))
        .writeToFile(File(mockDir).child("SampleOne"))
  }

  @Test
  fun testYelp() {
    val file = this::class.java.classLoader.getResource("yelp.graphqls")
    QCompiler.initialize("YelpGraphql")
        .packageName("com.prestongarno.ktq.yelp")
        .compile(File(file.path))
        .result {}
        .writeToFile(File(mockDir).child("YelpGraphql"))
  }
}

