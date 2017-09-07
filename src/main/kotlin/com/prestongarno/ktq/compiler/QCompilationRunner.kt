package com.prestongarno.ktq.compiler

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Actual task which lazy loads the values from config
 */
open class QCompilationRunner : DefaultTask() {
  val config = ConfigAdapter(lazy { QContext.configuration })
  override fun getDescription(): String = "convert graphql schema to kotlin"

  @get:OutputFile val output =
      project.provider({ config.targetDir.child("${config.packageName.replace(".", "/")}/$config.kotlinName")  })

  @get:Input val schemaFuture = project.provider({ config.schema })
  @get:Input val packFuture = project.provider({ config.packageName })
  @get:Input val kotFuture = project.provider({ config.kotlinName })

  @TaskAction fun ktqCompile() {
    if (schemaFuture.get().canRead()
        && schemaFuture.get().absolutePath.startsWith(project.rootDir.absolutePath)) {
      project.logger.info("generating graphql schema for target: ${config.schema}")
      QCompiler.initialize()
          .packageName(packFuture.get())
          .compile(schemaFuture.get())
          .writeToFile(output.get())
      this.didWork = true
    } else {
      project.logger.info("no graphql schema specified, skipping")
    }
  }
}

