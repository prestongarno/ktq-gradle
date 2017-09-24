package com.prestongarno.ktq

import com.prestongarno.ktq.compiler.asFile
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KClass

object JvmCompile {

  fun exe(input: File, output: File): Boolean = K2JVMCompiler().run {
    val args = K2JVMCompilerArguments().apply {
      freeArgs = mutableListOf(input.absolutePath)
      loadBuiltInsFromDependencies = true
      destination = output.absolutePath
      classpath = System.getProperty("java.class.path")
          .split(System.getProperty("path.separator"))
          .filter {
            it.asFile().exists() && it.asFile().canRead()
          }.joinToString(":")
      noStdlib = true
      noReflect = true
      skipRuntimeVersionCheck = true
      reportPerf = true
    }
    output.deleteOnExit()
    execImpl(
        PrintingMessageCollector(
            java.io.PrintStream(File("/dev/null")),
            MessageRenderer.WITHOUT_PATHS, true),
        Services.EMPTY,
        args)
  }.code == 0
}

fun File.getFileTree(): List<File> {
  return this.walkTopDown().asSequence().distinct()
      .filter { it.isFile }
      .toList()
}

class KtqCompileWrapper(private val root: File) {

  val loader = URLClassLoader(
      listOf(root.toURI().toURL()).toTypedArray(),
      this::class.java.classLoader)

  @Suppress("UNCHECKED_CAST") fun loadObject(name: String): QSchemaType =
      (loader.loadClass(name).kotlin as KClass<QSchemaType>).objectInstance!!

  @Suppress("UNCHECKED_CAST") fun loadInterface(name: String): KClass<QSchemaType> =
      (loader.loadClass(name).kotlin as KClass<QSchemaType>)
}
