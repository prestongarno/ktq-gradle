package com.prestongarno.ktq.compiler

import org.apache.log4j.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.internal.logging.services.DefaultLoggingManager
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import java.io.File

class GradleTests {
  val log: Logger by QContext.logger<GradleTests>()
  @Test
  fun testPluginConfiguration() {
/*    val mock = ProjectBuilder.builder()
        .withName("testone")
        .withGradleUserHomeDir(File(System.getProperty("user.home")))
        .withProjectDir(File("${TestContext.outputRoot.path}/testone"))
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
    val task: QCompilationRunner = mock.tasks.getByName("compileGraphql") as QCompilationRunner

    // set the schema target
    extension.schemaProp
        .set(File(TestContext.outputRoot.path)
            .child("testone/test.graphqls"))

    // execute the task
    task.actions.forEach {
      it.execute(task)
    }*/
  }

  @Test
  fun testCompileIncludeKotlin() {
    val projectRoot = File("${TestContext.outputRoot.path}/testone")
    val mock = ProjectBuilder.builder()
        .withName("testone")
        .withGradleUserHomeDir(File(System.getProperty("user.home")))
        .withProjectDir(projectRoot)
        .build()

    mock.plugins.apply(JavaPlugin::class.java)
    mock.plugins.apply(CompilerPlugin::class.java)

    // get the extension and the task
    val extension = mock.extensions.getByType(QCompilerConfig::class.java)!!
    //(mock.tasks.getByName("clean") as Delete).execute()
    val task: QCompilationRunner = mock.tasks.getByName("compileGraphql") as QCompilationRunner
    val assemble: JarBuilder = mock.tasks.getByName("assembleGraphql") as JarBuilder
    // set the schema target
    extension.schemaProp
        .set(File(TestContext.outputRoot.path)
                 .child("testone/test.graphqls"))
    mock.tasks.getByName("build").let { build ->
      build.doLast { t -> log.info("build task state ::" + t.state) }
      build.actions.forEach { it.execute(build) }
      (build as DefaultTask).execute()
    }
    task.ktqCompile()
  }
}

