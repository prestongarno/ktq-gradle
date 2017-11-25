package com.prestongarno.ktq.compiler

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ToKotlin {

  // yay!
  @Test fun `simple type to kotlin`() {
    val mockSchema = """
      |
      |type User {
      |  name: String
      |}
    """.trimMargin("|")

    compileOut(mockSchema) eq """
      |object User : QType {
      |  val name: StringDelegate.Query by QSchemaType.QScalar.String.stub()
      |}
      |""".trimMargin("|")
  }

  @Test fun `two field with type field`() {
    val result = compileOut("""
      |
      |type User {
      |  value: Float
      |  friends: [User]
      |}
      |""".trimMargin("|"), includeImports = false)

    val expect = """|
       |
       |object User : QType {
       |  val value: FloatDelegate.Query by QSchemaType.QScalar.Float.stub()
       |
       |  val friends: TypeListStub.Query<User> by QSchemaType.QTypeList.stub<User>()
       |}
       |""".trimMargin("|")

    assertThat(result)
        .isEqualTo(expect)
  }

}

