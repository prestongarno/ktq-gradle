package com.prestongarno.ktq.compiler

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import java.io.File

interface QConfig {
  val schema: File
  val targetDir: File
  val packageName: String
  val kotlinName: String
}

/**
 * Extension which is added by plugin to capture input
 */
open class QCompilerConfig(val project: Project) {
  @JvmField @Input  var schema = project.property(File::class.java)
  @JvmField @OutputDirectory var targetDir = project.property(File::class.java)
  @JvmField var packageName = project.property(String::class.java)
  @JvmField var kotlinName = project.property(String::class.java)
}

/**
 * Adapter which sets default values if they aren't specified
 */
class ConfigAdapter(configLoader: Lazy<QCompilerConfig>) : QConfig {
  private val configuration by configLoader
  @get:Input override val schema: File by lazy {
    val prop = configuration.schema
    (if (configuration.schema.isPresent
        && prop.get().exists()
        && prop.get().isFile
        && prop.get().canRead())
      prop.get()
    else
      File.createTempFile("null", "null").apply { setReadable(false) })
  }
  @get:OutputDirectory override val targetDir: File by lazy {
    configuration.targetDir.run {
      if (isPresent) get() else File("${QContext.project.buildDir.absolutePath}/generated/ktq/") }
  }
  override val packageName: String by lazy {
    val value = configuration.packageName
    (if (value.isPresent && value.get().isNotEmpty()) value.get() else "com.prestongarno.ktq.schema")
  }
  override val kotlinName: String by lazy {
    configuration.kotlinName.run {
      if (isPresent && get().isNotEmpty())
        get().toString().run {
          if (endsWith(".kt")) this else "$this.kt" }
      else configuration.schema.get().nameWithoutExtension
          .toJavaFileCompat()
    }
  }
}
