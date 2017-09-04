package com.prestongarno.ktq.compiler

import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.URI
import kotlin.reflect.jvm.jvmName

class GradleTests {

  lateinit var mockDir: URI
  val SCHEMA_NAME = "test.graphqls"

  @Before
  fun setUp() {
    mockDir = this::class.java
        .classLoader
        .getResource(System.getProperty("-D" + this::class.jvmName))
        .toURI() ?: throw IllegalStateException("Mock project root not set!")
  }

  @Test
  fun testPluginConfiguration() {
    val mock = ProjectBuilder.builder()
        .withName("01")
        .withProjectDir(File("${mockDir.path}/01"))
        .build()

    // add the plugin
    mock.plugins.apply(JavaPlugin::class.java)
    mock.plugins.apply(KotlinPlatformJvmPlugin::class.java)
    mock.plugins.apply(CompilerPlugin::class.java)
    // check for the buildscript
    require(mock.buildscript.sourceFile.exists())
    // check the plugin
    require(mock.plugins.find { it is CompilerPlugin } != null)
    // check that the extension is created
    require(mock.extensions.findByType(QCompilerConfig::class.java) != null)
    val extension = mock.extensions.getByType(QCompilerConfig::class.java)!!

    val task: QCompilationRunner = mock.tasks.getByName("ktqCompile") as QCompilationRunner

    // set the schema target
    extension.schemaProp
        .set(File(mockDir)
            .child("01/$SCHEMA_NAME"))

    // execute the task
    task.actions.forEach {
      it.execute(task) }

    // check the result
  }
}

