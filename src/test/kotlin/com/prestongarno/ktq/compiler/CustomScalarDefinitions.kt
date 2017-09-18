package com.prestongarno.ktq.compiler

import org.junit.Test
import kotlin.test.assertTrue
import com.google.common.truth.Truth.assertThat
import com.prestongarno.ktq.CustomScalar
import com.prestongarno.ktq.CustomScalarInitStub
import com.prestongarno.ktq.CustomScalarListInitStub
import com.prestongarno.ktq.JvmCompile
import com.prestongarno.ktq.KtqCompileWrapper
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

const val PACK: String = "com.test"


class CustomScalarDefinitions : BaseTest() {

  @Test fun testSingleCustomScalarDefinition() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |scalar URL
          |
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |object URL : CustomScalar
                |
                """.trimMargin("|"))
        }
  }

  @Test fun testSingleFieldOfCustomScalarType() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |scalar URL
          |
          |type Foo {
          |  url: URL
          |}
          |
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |object Foo : QSchemaType {
                |  val url: CustomScalarInitStub<URL> by QCustomScalar.stub()
                |}
                |
                |object URL : CustomScalar
                |
                """.trimMargin("|"))
        }
        .writeToFile(codegenOutputFile)

    assertTrue(JvmCompile.exe(codegenOutputFile, compileOutputDir))

    KtqCompileWrapper(compileOutputDir).run {
      val urlObject = loadObject("$PACK.URL")
      val fooObject = loadObject("$PACK.Foo")

      assertThat(fooObject::class.declaredMemberProperties.find { it.name == "url" }?.run {
        assertThat(this.returnType.jvmErasure).isEqualTo(CustomScalarInitStub::class)
      } != null)
      assertThat(urlObject).isInstanceOf(CustomScalar::class.java)
    }
  }

  @Test fun testSingleFieldOfCustomScalarListType() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |scalar URL
          |
          |type Product {
          |  relatedLinks: [URL]
          |}
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |object Product : QSchemaType {
                |  val relatedLinks: CustomScalarListInitStub<URL> by QCustomScalarList.stub()
                |}
                |
                |object URL : CustomScalar
                |
                """.trimMargin("|"))
        }.writeToFile(codegenOutputFile)

    assertTrue(JvmCompile.exe(codegenOutputFile, compileOutputDir))

    KtqCompileWrapper(compileOutputDir).run {
      val urlObject = loadObject("$PACK.URL")
      val productObject = loadObject("$PACK.Product")


      assertThat(productObject::class.declaredMemberProperties.find { it.name == "relatedLinks" }?.run {
        assertThat(this.returnType.jvmErasure).isEqualTo(CustomScalarListInitStub::class)
      } != null)
      assertThat(urlObject).isInstanceOf(CustomScalar::class.java)
    }
  }

  @Test fun testListCustomScalarTakingInputArguments() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |scalar URL
          |
          |type Product {
          |  relatedLinks(
          |    first: Int,
          |    last: Int,
          |    startingAt: URL,
          |    associationFactor: Float
          |  ): [URL]
          |}
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
          |object Product : QSchemaType {
          |  val relatedLinks: CustomScalarListConfigStub<URL, RelatedLinksArgs> by QCustomScalarList.configStub { RelatedLinksArgs(it) }
          |
          |  class RelatedLinksArgs(args: CustomScalarListArgBuilder) : CustomScalarListArgBuilder by args {
          |    fun first(value: Int): RelatedLinksArgs = apply { addArg("first", value) }
          |
          |
          |    fun last(value: Int): RelatedLinksArgs = apply { addArg("last", value) }
          |
          |
          |    fun startingAt(value: URL): RelatedLinksArgs = apply { addArg("startingAt", value) }
          |
          |
          |    fun associationFactor(value: Float): RelatedLinksArgs = apply { addArg("associationFactor", value) }
          |
          |  }
          |}
          |
          |object URL : CustomScalar
          |""".trimMargin("|"))
        }
        .writeToFile(codegenOutputFile)

    assertTrue(JvmCompile.exe(codegenOutputFile, compileOutputDir))
  }

  @Test fun testSingleCustomScalarInputArgs() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |scalar URL
          |
          |type Product {
          |  webLink(
          |    shortened: Boolean
          |  ): URL
          |}
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |object Product : QSchemaType {
                |  val webLink: CustomScalarConfigStub<URL, WebLinkArgs> by QCustomScalar.configStub { WebLinkArgs(it) }
                |
                |  class WebLinkArgs(args: CustomScalarArgBuilder) : CustomScalarArgBuilder by args {
                |    fun shortened(value: Boolean): WebLinkArgs = apply { addArg("shortened", value) }
                |
                |  }
                |}
                |
                |object URL : CustomScalar
                |""".trimMargin("|"))
        }.writeToFile(codegenOutputFile)
    assertTrue(JvmCompile.exe(codegenOutputFile, compileOutputDir))
  }

  @Test fun testMultipleInheritanceFieldsWithCustomScalarTypes() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |interface Node {
          |  url(shortened: Boolean): URL
          |}
          |
          |scalar URL
          |
          |interface UniformResourceLocatable {
          |  size: Int
          |  url(shortened: Boolean): URL
          |}
          |
          |type User implements Node, UniformResourceLocatable {
          |  size: Int
          |  url(shortened: Boolean, encoding: String): URL
          |}
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |abstract class BaseUrlArgs(args: CustomScalarArgBuilder) : CustomScalarArgBuilder by args
                |
                |interface Node : QSchemaType {
                |  val url: CustomScalarConfigStub<URL, BaseUrlArgs>
                |}
                |
                |object URL : CustomScalar
                |
                |interface UniformResourceLocatable : QSchemaType {
                |  val size: Stub<Int>
                |
                |  val url: CustomScalarConfigStub<URL, BaseUrlArgs>
                |}
                |
                |object User : QSchemaType, UniformResourceLocatable, Node {
                |  override val size: Stub<Int> by QScalar.stub()
                |
                |  override val url: CustomScalarConfigStub<URL, UrlArgs> by QCustomScalar.configStub { UrlArgs(it) }
                |
                |  class UrlArgs(args: CustomScalarArgBuilder) : BaseUrlArgs(args) {
                |    fun shortened(value: Boolean): UrlArgs = apply { addArg("shortened", value) }
                |
                |
                |    fun encoding(value: String): UrlArgs = apply { addArg("encoding", value) }
                |
                |  }
                |}
                |
                """.trimMargin("|"))
        }
        .writeToFile(codegenOutputFile)

    assertTrue(JvmCompile.exe(codegenOutputFile, compileOutputDir))
  }
  @Test fun testMultipleInheritanceListFieldWithInput() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |interface Node {
          |  urls(shortened: Boolean): [URL]
          |}
          |
          |scalar URL
          |
          |interface UniformResourceLocatable {
          |  size: Int
          |  urls(shortened: Boolean): [URL]
          |}
          |
          |type User implements Node, UniformResourceLocatable {
          |  size: Int
          |  urls(shortened: Boolean, encoding: String): [URL]
          |}
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |abstract class BaseUrlsArgs(args: CustomScalarListArgBuilder) : CustomScalarListArgBuilder by args
                |
                |interface Node : QSchemaType {
                |  val urls: CustomScalarListConfigStub<URL, BaseUrlsArgs>
                |}
                |
                |object URL : CustomScalar
                |
                |interface UniformResourceLocatable : QSchemaType {
                |  val size: Stub<Int>
                |
                |  val urls: CustomScalarListConfigStub<URL, BaseUrlsArgs>
                |}
                |
                |object User : QSchemaType, UniformResourceLocatable, Node {
                |  override val size: Stub<Int> by QScalar.stub()
                |
                |  override val urls: CustomScalarListConfigStub<URL, UrlsArgs> by QCustomScalarList.configStub { UrlsArgs(it) }
                |
                |  class UrlsArgs(args: CustomScalarListArgBuilder) : BaseUrlsArgs(args) {
                |    fun shortened(value: Boolean): UrlsArgs = apply { addArg("shortened", value) }
                |
                |
                |    fun encoding(value: String): UrlsArgs = apply { addArg("encoding", value) }
                |
                |  }
                |}
                |
                """.trimMargin("|"))
        }
        .writeToFile(codegenOutputFile)

    assertTrue(JvmCompile.exe(codegenOutputFile, compileOutputDir))
  }

  @Test fun testCustomScalarTypesSchemaWithEnum() {
    QCompiler.initialize()
        .packageName(PACK)
        .schema("""
          |
          |enum TicketStatus {
          |  OPEN,
          |  IN_PROGRESS,
          |  CLOSED
          |}
          |
          |scalar URL
          |
          |type Ticket {
          |  status: TicketStatus
          |  url: URL
          |}
          """.trimMargin("|"))
        .compile()
        .result {
          assertThat(it.minusMetadata())
              .isEqualTo("""
                |object Ticket : QSchemaType {
                |  val status: Stub<TicketStatus> by QScalar.stub()
                |
                |  val url: CustomScalarInitStub<URL> by QCustomScalar.stub()
                |}
                |
                |enum class TicketStatus : QSchemaType {
                |  OPEN,
                |
                |  IN_PROGRESS,
                |
                |  CLOSED
                |}
                |
                |object URL : CustomScalar
                |""".trimMargin("|"))
        }.writeToFile(codegenOutputFile)

    assertTrue(JvmCompile.exe(codegenOutputFile, compileOutputDir))
  }
}


