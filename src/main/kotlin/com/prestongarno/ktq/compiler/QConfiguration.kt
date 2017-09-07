package com.prestongarno.ktq.compiler

import org.gradle.api.Project
import org.gradle.api.tasks.Input
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
  @JvmField var schema = project.property(File::class.java)
  @JvmField var targetDir = project.property(File::class.java)
  @JvmField var packageName = project.property(String::class.java)
  @JvmField var kotlinName = project.property(String::class.java)
  fun setSchema(value: File) = schema.set(value)
  fun setTargetDir(value: File) = targetDir.set(value)
  fun setPackageName(value: String) = packageName.set(value)
  fun setKotlinName(value: String) = kotlinName.set(value)

  @Input
  fun getSchema() = schema.get()

  @Input
  fun getTargetDir() = targetDir.get()

  @Input
  fun getPackageName() = packageName.get()

  @Input
  fun getKotlinName() = kotlinName.get()
}

/**
 * Adapter which sets default values if they aren't specified
 */
class ConfigAdapter(val config: Lazy<QCompilerConfig>) : QConfig {
  override val schema: File by lazy {
    val prop = config.value.schema
    (if (config.value.schema.isPresent
        && prop.get().exists()
        && prop.get().isFile
        && prop.get().canRead())
      prop.get()
    else
      File.createTempFile("null", "null").apply { setReadable(false) })
  }
  override val targetDir: File by lazy {
    config.value.targetDir.let {
      if (it.isPresent)
        it.get()
      else
        File("${config.value.project.buildDir.absolutePath}/generated/ktq/")
    }
  }
  override val packageName: String by lazy {
    val value = config.value.packageName
    (if (value.isPresent && value.get().isNotEmpty())
      value.get()
    else
      "com.prestongarno.ktq.schema")
  }
  override val kotlinName: String by lazy {
    val value = config.value.kotlinName
    (if (value.isPresent && value.get().isNotEmpty())
      value.get()
    else config.value.schema.run {
      if (isPresent) get().nameWithoutExtension.toJavaFileCompat() else "GraphTypes"
    })
  }
}
