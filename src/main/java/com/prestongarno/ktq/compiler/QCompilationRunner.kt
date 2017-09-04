package com.prestongarno.ktq.compiler

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class QCompilationRunner : DefaultTask(), QConfig by ConfigAdapter(QContext.configuration) {

  @TaskAction
  fun ktqCompile() {
    if (schema.canRead()) {
      project.logger
          .info("generating graphql schema for target: $schema")
      QCompiler.initialize()
          .packageName(packageName)
          .compile(schema)
          .writeToFile(targetDir.child(kotlinName).absolutePath)
    } else {
      project.logger
          .info("no graphql schema specified, skipping")
    }
  }
}