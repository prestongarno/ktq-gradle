package com.prestongarno.ktq.compiler

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class QCompilationRunner : DefaultTask(), QConfig by ConfigAdapter(Context.configuration) {

  @TaskAction
  fun ktqCompile() {

    if (schema.canRead()) {
      project.logger
          .info("generating graphql schema for target: $schema")
      QCompiler.initialize()
          .packageName(packageName)
          .compile(schema) //{ println(it.getAllTypes().joinToString(separator = "\n" + "=".repeat(20), prefix = "=".repeat(40) + "\n\n")) }
          .writeToFile(targetDir.child(kotlinName).absolutePath)
    } else {
      project.logger
          .info("no graphql schema specified, skipping")
    }
  }
}