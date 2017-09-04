package com.prestongarno.ktq.compiler

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

open class CompilerPlugin : Plugin<Project> {

  override fun apply(project: Project?) {
    QContext.project = project!!
    project.extensions?.create("ktq", QCompilerConfig::class.java, project)
    val runner = project.tasks.create("compileGraphql", QCompilationRunner::class.java)
    //project.tasks.create("assembleGraphql", JarBuilder::class.java)
    project.tasks.filter { it.group?.contains("org.jetbrains") == true }
        .forEach { it.dependsOn.add(runner) }
  }
}


fun String.toJavaFileCompat(): String {
  return replace("[^a-z|A-Z|0-9]*".toRegex(), "")
      .let {
        val firstLetter = it.indexOfFirst {
          it.toString().matches("[a-zA-Z]".toRegex())
        }
        if (firstLetter > -1)
          it.substring(firstLetter)
        else throw IllegalArgumentException("Can not generate schema for '$this': Please use a " +
            "java-compatible name for your schema or specify a name in your buildscript")
      }
}

fun File.child(relative: String): File = File("$absolutePath/$relative")

fun String.asFile(): File = File(this)

fun String.containsAny(match: List<String>): Boolean = match.find { contains(it) } != null
