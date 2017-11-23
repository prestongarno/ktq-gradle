package com.prestongarno.ktq.compiler

import com.prestongarno.ktq.ArgBuilder
import com.prestongarno.ktq.CustomScalar
import com.prestongarno.ktq.CustomScalarArgBuilder
import com.prestongarno.ktq.CustomScalarConfigStub
import com.prestongarno.ktq.CustomScalarInitStub
import com.prestongarno.ktq.CustomScalarListArgBuilder
import com.prestongarno.ktq.CustomScalarListConfigStub
import com.prestongarno.ktq.CustomScalarListInitStub
import com.prestongarno.ktq.CustomScalarListStub
import com.prestongarno.ktq.InitStub
import com.prestongarno.ktq.ListConfig
import com.prestongarno.ktq.ListConfigType
import com.prestongarno.ktq.ListInitStub
import com.prestongarno.ktq.ListStub
import com.prestongarno.ktq.QConfigStub
import com.prestongarno.ktq.QInput
import com.prestongarno.ktq.QSchemaType
import com.prestongarno.ktq.QTypeConfigStub
import com.squareup.kotlinpoet.KotlinFile
import java.io.File
import kotlin.reflect.KClass

class QCompiler internal constructor(builder: Builder) {
  private val packageName = builder.packageName
  private lateinit var compilation: QCompilationUnit
  private var rawResult: String = ""

  internal constructor(builder: Builder, consumer: (QCompilationUnit) -> Unit) : this(builder) {
    compilation = Attr.attributeCompilationUnit(
        (if (builder.schemaFile != null)
          QLParser.parse(builder.schemaFile!!)
        else QLParser.parse(builder.schemaValue?: throw IllegalStateException("No input."))))
    this.result { compilation }
    consumer(this.compilation)
  }

  companion object {
    fun initialize() = Builder()
    // String literals and replacement because of missing kotlinpoet features
    val LESS_THAN = "LESS_THAN"
    val GREATER_THAN = "GREATER_THAN"
    val COMMA = "_COMMA_"
  }

  fun result(consumer: (String) -> Unit) = apply {
    if (rawResult.isNotEmpty()) {
      consumer(rawResult)
      return this
    }
    val ktBuilder = KotlinFile.builder(packageName, "YouShouldntSeeThis")

    getResolvedImports().mapNotNull {
      it.simpleName
    }.let { ktBuilder.addStaticImport("com.prestongarno.ktq", *it.toTypedArray()) }

    getSchemaTypeHelpers().mapNotNull {
      it.simpleName
    }.let { ktBuilder.addStaticImport(com.prestongarno.ktq.QSchemaType::class, *it.toTypedArray()) }

    compilation
        .getAllTypes()
        .forEach {
          ktBuilder.addType(it)
        }

    val suppressedWarnings = listOf(
        "@file:Suppress(\"unused\")"
    )

    val rawFile = ktBuilder.build().toString()

    val result = suppressedWarnings.joinToString("\n") +
        "\n\n" +
        (rawFile.replace("ArgBuilder(.*)_by_args".toRegex(), "ArgBuilder$1 by args")
            .replace(LESS_THAN, "<")
            .replace(GREATER_THAN, ">")
            .replace(COMMA, ", ")
            .replace("> \\{\n.*stub\\((.*)\\)\n.*}".toRegex(), "> = stub($1)"))
            .replace(" = null\n".toRegex(), "? = null")
            .replace("\\nimport class(.*)by args".toRegex(), "\nimport$1")
    this.rawResult = result
    consumer(result)
  }

  fun writeToFile(destination: String) = apply {
    val outResolved =
        if (destination.endsWith(".kt"))
          destination
        else destination + ".kt"

    writeToFile(File("$destination/$outResolved"))
  }

  fun writeToFile(destination: File) = apply {
    destination.parent.asFile().let {
      if (!it.exists()) it.mkdirs()
    }
    destination.printWriter()
        .use { out -> out.write(rawResult) }
  }



  class Builder internal constructor() {
    internal var packageName: String = "com.prestongarno.ktq"
    internal var schemaFile: File? = null
    internal var schemaValue: String? = null

    fun packageName(name: String) = apply { this.packageName = name }
    fun schema(file: File) = apply { this.schemaFile = file }
    fun schema(value: String) = apply { this.schemaValue = value }
    fun compile(): QCompiler = QCompiler(this) {  }
    fun compile(consumer: (QCompilationUnit) -> Unit): QCompiler = QCompiler(this, consumer)
  }
}

private fun getResolvedImports(): List<KClass<*>> {
  return listOf(
      ArgBuilder::class,
      InitStub::class,
      ListConfig::class,
      ListConfigType::class,
      ListInitStub::class,
      ListStub::class,
      QConfigStub::class,
      QInput::class,
      QSchemaType::class,
      QTypeConfigStub::class,
      CustomScalar::class,
      CustomScalarInitStub::class,
      CustomScalarListInitStub::class,
      CustomScalarListStub::class,
      CustomScalarArgBuilder::class,
      CustomScalarListArgBuilder::class,
      CustomScalarConfigStub::class,
      CustomScalarListConfigStub::class
  )
}

private fun getSchemaTypeHelpers(): List<KClass<*>> =
    listOf(
        QSchemaType.QScalar::class,
        QSchemaType.QScalarArray::class,
        QSchemaType.QType::class,
        QSchemaType.QTypeList::class,
        QSchemaType.QCustomScalar::class,
        QSchemaType.QCustomScalarList::class
    )

