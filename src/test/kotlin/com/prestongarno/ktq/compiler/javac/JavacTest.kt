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

  private var compileClassLoader: KtqCompileWrapper? = null

  @After fun tearDown() {
    compileClassLoader?.delete()
  }

  protected fun compileAndLoad(
      schema: String,
      packageName: String = "",
      printer: PrintStream? = null,
      block: GraphQLCompiler.() -> Unit = { }
  ): KtqCompileWrapper {

    val buildOut = Files.createTempDir().absolutePath.plus(
        packageName.split(".").joinToString(separator = "/")
    ).asFile().apply(File::mkdirsOrFail)

    val kotlinOut = File.createTempFile(
        "Kotlinpoet${Instant.now().toEpochMilli()}",
        ".kt",
        buildOut
    )
        .apply(File::deleteOnExit)
    val compilation = GraphQLCompiler(schema = StringSchema(schema))
        .apply(GraphQLCompiler::compile)
        .apply(block)
    val spec = FileSpec.builder(packageName, "")
    compilation.definitions.map(SchemaType<*>::toKotlin)
        .forEach { spec.addType(it) }
    spec.build().toString().apply { kotlinOut.writeText(this@apply) }
    return JvmCompile.exe(kotlinOut, Files.createTempDir().apply(File::deleteOnExit), printer)
  }
}
