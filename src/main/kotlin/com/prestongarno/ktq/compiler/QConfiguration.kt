package com.prestongarno.ktq.compiler

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import java.io.File

interface QConfig {
  @get:Input val schema: File
  @get:OutputDirectory val targetDir: File
  val packageName: String
  val kotlinName: String
}

open class QCompilerConfig(val project: Project) {
  val schemaProp = project.property(File::class.java)
  val targetDirProp = project.property(File::class.java)
  var packageNameProp = project.property(String::class.java)
  var kotlinNameProp = project.property(String::class.java)
}

class ConfigAdapter(private val configuration: QCompilerConfig) : QConfig {

  override val schema: File by lazy {
    val prop = configuration.schemaProp
    (if (configuration.schemaProp.isPresent
        && prop.get().exists()
        && prop.get().isFile
        && prop.get().canRead()) prop.get()
    else File.createTempFile("null", "null").apply { setReadable(false) })
        .also { println("Schema file for config at: $it") }
  }
  override val targetDir: File by lazy {
    val prop = configuration.targetDirProp
    (if (prop.isPresent
        && prop.get().exists()
        && prop.get().isDirectory
        && prop.get().canExecute()) prop.get()
    else
      File("${QContext.project.buildDir.absolutePath}/generated/ktq/"))
        .also { println("Target directory for schema at: $it") }
  }
  override val packageName: String by lazy {
    val value = configuration.packageNameProp
    (if (value.isPresent && value.get().isNotEmpty())
      value.get()
    else
      "com.prestongarno.ktq.schema")
        .also { println("Package name for schema: $it") }
  }
  override val kotlinName: String by lazy {
    val value = configuration.kotlinNameProp
    (if (value.isPresent && value.get().isNotEmpty())
      value.get()
    else configuration.schemaProp.get().nameWithoutExtension
        .toJavaFileCompat())
        .also { println("Kotlin file name = '$it'")}
  }
}
