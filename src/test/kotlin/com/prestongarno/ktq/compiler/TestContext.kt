package com.prestongarno.ktq.compiler

import java.io.File
import java.net.URI

object TestContext {
  private val outputJvmArg = System.getProperty("com.prestongarno.ktq.compiler.testOutput")
  val outputRoot: File = File(outputJvmArg)
}

