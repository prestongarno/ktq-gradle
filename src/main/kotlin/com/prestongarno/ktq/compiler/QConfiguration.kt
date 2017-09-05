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

/**
 * Extension which is added by plugin to capture input
 */
open class QCompilerConfig(project: Project) {
  /** Actually, I don't even know why these are a thing on second thought
   */
  var schemaProp = project.property(File::class.java)
  var targetDirProp = project.property(File::class.java)
  var packageNameProp = project.property(String::class.java)
  var kotlinNameProp = project.property(String::class.java)

  fun schema(value: String) = schemaProp.set(value.asFile())
  fun targetDir(value: String) = targetDirProp.set(value.asFile())
  fun packageName(value: String) = packageNameProp.set(value)
  fun kotlinName(value: String) = kotlinNameProp.set(value)
}

/**
 * Adapter which sets default values if they aren't specified
 */
class ConfigAdapter(configLoader: Lazy<QCompilerConfig>) : QConfig {

  private val configuration by configLoader

  override val schema: File by lazy {
    val prop = configuration.schemaProp
    (if (configuration.schemaProp.isPresent
        && prop.get().exists()
        && prop.get().isFile
        && prop.get().canRead())
      prop.get()
    else
      File.createTempFile("null", "null").apply { setReadable(false) })
  }
  override val targetDir: File by lazy {
    configuration.targetDirProp.let {
      if(it.isPresent)
        it.get()
      else
        File("${QContext.project.buildDir.absolutePath}/generated/ktq/") }
  }

  override val packageName: String by lazy {
    val value = configuration.packageNameProp
    (if (value.isPresent && value.get().isNotEmpty())
      value.get()
    else
      "com.prestongarno.ktq.schema")
  }
  override val kotlinName: String by lazy {
    val value = configuration.kotlinNameProp
    (if (value.isPresent && value.get().isNotEmpty())
      value.get()
    else configuration.schemaProp.get().nameWithoutExtension
        .toJavaFileCompat())
  }
}
