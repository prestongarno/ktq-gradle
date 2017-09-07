package com.prestongarno.ktq.compiler

import org.junit.Test
import java.io.File
import kotlin.test.fail

class HierarchyAttr {

  fun loadSchema(name: String) = File(this::class.java.classLoader.getResource(name).toURI())

  @Test fun failInheritedFieldWrongType() {
    try {
      loadSchema("test01.graphqls").let { file ->
        QCompiler.initialize()
            .compile(file)
      }
      fail()
    } catch (expected: IllegalArgumentException) {
      require(expected.message?.contains("Incompatible types") == true)
    }
  }
}