package com.prestongarno.ktq.compiler

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.Serializable

/** Actual task which lazy loads the values from config
 */
open class QCompilationRunner : DefaultTask(), Serializable {

  override fun getDescription(): String = "convert graphql schema to kotlin"

    @TaskAction fun ktqCompile() {
      ConfigAdapter(lazy {project.extensions.findByType(QCompilerConfig::class.java)}).run {
        if (schema.canRead() && schema.absolutePath.startsWith(project.rootDir.absolutePath)) {
          project.logger.info("generating graphql schema for target: " +
                                  "${QContext.configuration(project).schema}")

          QCompiler.initialize()
              .packageName(packageName)
              .schema(schema)
              .compile()
              .writeToFile(targetDir.child("${packageName.replace(".", "/")}/${kotlinName}"))

        } else {
          project.logger.info("no graphql schema specified skipping")
        }
      }
    }

    private fun writeObject(out: java.io.ObjectOutputStream) { out.defaultWriteObject() }

    private fun readObject(inStream: java.io.ObjectInputStream) { inStream.defaultReadObject() }

    private fun readObjectNoData() {}
}

