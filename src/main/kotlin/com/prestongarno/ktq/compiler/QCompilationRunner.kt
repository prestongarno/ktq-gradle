package com.prestongarno.ktq.compiler

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.io.File

/**
 * Actual task which lazy loads the values from config
 */
open class QCompilationRunner : DefaultTask(),
    QConfig by ConfigAdapter(lazy { QContext.configuration }) {

  override fun getDescription(): String = "convert graphql schema to kotlin"

  val output by lazy { targetDir.child(kotlinName) }

  @TaskAction fun ktqCompile() {
    if (schema.canRead()
        && schema.absolutePath.startsWith(project.rootDir.absolutePath)) {
      project.logger.info("generating graphql schema for target: $schema")
      QCompiler.initialize()
          .packageName(packageName)
          .compile(schema)
          .writeToFile(output.absolutePath)
      this.didWork = true
    } else {
      project.logger.info("no graphql schema specified, skipping")
    }
  }
}

open class JarBuilder : Jar(),
    QConfig by ConfigAdapter(lazy { QContext.configuration }) {

  val customDest: File by lazy { targetDir.child("/artifacts") }

  @TaskAction fun exe() {
    this.from(this.targetDir)
        .into(destinationDir)
  }

  @Input override fun getSource(): FileCollection {
    return project.fileTree(targetDir)
  }

  @OutputDirectory override fun getDestinationDir(): File {
    val checkIfSpecified = super.getDestinationDir() ?: customDest
    return if (checkIfSpecified == project.findProperty("distsDir"))
      customDest
    else checkIfSpecified
  }
}
