package com.prestongarno.ktq.compiler

import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.junit.Before
import org.junit.Test
import java.io.File

class GradleTests {

  @Test
  fun testPluginConfiguration() {
    val mock = ProjectBuilder.builder()
        .withName("01")
        .withGradleUserHomeDir(File(System.getProperty("user.home")))
        .withProjectDir(File("${TestContext.outputRoot.path}/01"))
        .build()

    // add the plugin
    mock.plugins.apply(JavaPlugin::class.java)
    mock.plugins.apply(KotlinPlatformJvmPlugin::class.java)
    mock.plugins.apply(CompilerPlugin::class.java)
    // check the plugin
    require(mock.plugins.find { it is CompilerPlugin } != null)
    // check that the extension is created
    require(mock.extensions.findByType(QCompilerConfig::class.java) != null)

    // get the extension and the task
    val extension = mock.extensions.getByType(QCompilerConfig::class.java)!!
    val task: QCompilationRunner = mock.tasks.getByName("ktqCompile") as QCompilationRunner

    // set the schema target
    extension.schemaProp
        .set(File(TestContext.outputRoot.path)
            .child("01/test.graphqls"))

    // execute the task
    task.actions.forEach {
      it.execute(task) }
  }
}

