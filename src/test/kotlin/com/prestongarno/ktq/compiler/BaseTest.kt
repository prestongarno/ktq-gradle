package com.prestongarno.ktq.compiler

import org.jetbrains.kotlin.preprocessor.mkdirsOrFail
import org.junit.After
import org.junit.Before
import java.io.File
import java.util.concurrent.ThreadLocalRandom

open class BaseTest {
  lateinit var compileOutputDir: File
  lateinit var codegenOutputFile: File

  @Before fun setUp() {
    codegenOutputFile = File("${System.getProperty("java.io.tmpdir")}/KtqKotlin.kt")
    compileOutputDir = File(System.getProperty("java.io.tmpdir") +
        "/ktq-gradle-temp/${ThreadLocalRandom.current().nextLong()}").apply {
      mkdirsOrFail()
      deleteOnExit()
    }
  }

  @After fun tearDown() {
    codegenOutputFile.delete()
    compileOutputDir.deleteRecursively()
  }
}