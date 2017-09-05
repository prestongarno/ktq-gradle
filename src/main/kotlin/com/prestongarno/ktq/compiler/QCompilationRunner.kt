package com.prestongarno.ktq.compiler

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.io.File

open class QCompilationRunner : DefaultTask(), QConfig by ConfigAdapter(QContext.configuration) {

  val output by lazy { targetDir.child(kotlinName) }

  @TaskAction
  fun ktqCompile(): Boolean {
    return if (schema.canRead()) {
      project.logger.info("generating graphql schema for target: $schema")
      QCompiler.initialize()
          .packageName(packageName)
          .compile(schema)
          .writeToFile(output.absolutePath)
      this.didWork = true
      this.didWork
    } else {
      project.logger.info("no graphql schema specified, skipping")
      false
    }
  }
}

/*open class JarBuilder : Jar(), QConfig by ConfigAdapter(QContext.configuration) {
  val customDest: File by lazy { targetDir.child("/artifacts") }

  @TaskAction
  fun exe() {
    this.from(this.targetDir)
        .into(destinationDir)
        .actions.forEach { it.execute(this) }
  }

  override fun getSource(): FileCollection {
    return project.fileTree(targetDir)
  }

  override fun getDestinationDir(): File {
    val checkIfSpecified = super.getDestinationDir() ?: customDest
    return if (checkIfSpecified == project.findProperty("distsDir"))
      customDest
    else checkIfSpecified
  }
}*/
