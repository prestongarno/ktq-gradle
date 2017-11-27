package com.prestongarno.ktq.compiler.javac

import com.google.common.io.Files
import com.prestongarno.ktq.compiler.GraphQLCompiler
import com.prestongarno.ktq.compiler.JvmCompile
import com.prestongarno.ktq.compiler.KtqCompileWrapper
import com.prestongarno.ktq.compiler.SchemaType
import com.prestongarno.ktq.compiler.StringSchema
import com.prestongarno.ktq.compiler.asFile
import com.prestongarno.ktq.compiler.compileGraphQl
import com.prestongarno.ktq.compiler.println
import com.prestongarno.ktq.compiler.toFileSpec
import com.squareup.kotlinpoet.FileSpec
import org.jetbrains.kotlin.preprocessor.mkdirsOrFail
import org.junit.After
import java.io.File
import java.io.PrintStream
import java.time.Instant

open class JavacTest {

  protected fun jvmCompileAndLoad(
      schema: String,
      packageName: String = "",
      printer: PrintStream? = null,
      block: GraphQLCompiler.() -> Unit = { }
  ): KtqCompileWrapper {

    val tempDir = Files.createTempDir()

    val kotlinOut = File.createTempFile(
        "Kotlinpoet${Instant.now().toEpochMilli()}",
        ".kt",
        tempDir
    ).apply { deleteOnExit() }

    val compilation = GraphQLCompiler(schema = StringSchema(schema))
        .apply(GraphQLCompiler::compile)
        .apply(block)

    val spec = FileSpec.builder(packageName, kotlinOut.name)

    compilation.definitions.map(SchemaType<*>::toKotlin)
        .forEach { spec.addType(it) }

    spec.build().toString().apply {
      kotlinOut.writeText(this@apply)
    }
    return JvmCompile.exe(kotlinOut, tempDir, printer)
  }
}
