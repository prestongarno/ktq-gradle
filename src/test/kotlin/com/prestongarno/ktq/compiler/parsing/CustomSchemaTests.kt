package com.prestongarno.ktq.compiler.parsing

import com.prestongarno.ktq.compiler.QCompiler
import com.prestongarno.ktq.compiler.qlang.spec.QDirectiveSymbol
import com.prestongarno.ktq.compiler.qlang.spec.QField
import com.prestongarno.ktq.compiler.qlang.spec.QInt
import com.prestongarno.ktq.compiler.qlang.spec.QTypeDef
import com.prestongarno.ktq.compiler.qlang.spec.QUnknownType
import com.prestongarno.ktq.compiler.qlang.spec.Scalar
import java.io.File
import org.junit.Test

class CustomSchemaTests {
  @Test
  fun testConflictingOverridesPass() {
    val file = this::class.java.classLoader.getResource("sample.schema.graphqls")
    QCompiler.initialize()
        .packageName("com.prestongarno.ktq")
        .schema(File(file.path))
        .compile()
        .result { require(it.isNotEmpty()) }
  }

  @Test
  fun testYelp() {
    val file = this::class.java.classLoader.getResource("yelp.graphqls")
    QCompiler.initialize()
        .packageName("com.prestongarno.ktq.yelp")
        .schema(File(file.path))
        .compile()
        .result { require(it.isNotEmpty()) }
  }
}

