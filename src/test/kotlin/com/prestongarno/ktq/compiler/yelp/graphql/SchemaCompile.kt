package com.prestongarno.ktq.compiler.yelp.graphql

import com.prestongarno.ktq.compiler.KtqCompileWrapper
import com.prestongarno.ktq.compiler.eq
import com.prestongarno.ktq.compiler.ignore
import com.prestongarno.ktq.compiler.javac.JavacTest
import com.prestongarno.ktq.compiler.javac.kprop
import com.prestongarno.ktq.compiler.javac.func
import com.prestongarno.ktq.compiler.javac.requireReturns
import com.prestongarno.ktq.compiler.notEq
import com.prestongarno.ktq.stubs.BooleanDelegate
import com.prestongarno.ktq.stubs.FloatDelegate
import com.prestongarno.ktq.stubs.IntDelegate
import com.prestongarno.ktq.stubs.StringDelegate
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for compiled Yelp GraphQL schema
 *
 * Will primarily test this object from the Yelp GraphQL Schema:
 *
 * type Business {
 *    name: String
 *    id: String
 *    is_claimed: Boolean
 *    is_closed: Boolean
 *    url: String
 *    phone: String
 *    display_phone: String
 *    review_count: Int
 *    categories: [Category]
 *    rating: Float
 *    price: String
 *    location: Location
 *    coordinates: Coordinates
 *    photos: [String]
 *    hours: [Hours]
 *    reviews: [Review]
 * }
 */
class SchemaCompile : JavacTest() {


  /** classloader for objects & classes from the schema */
  lateinit var loader: KtqCompileWrapper

  @Before fun compileYelpSchemaAndLoad() {
    val schemaSource = this::class.java
        .classLoader
        .getResourceAsStream("yelp.graphqls")
        .reader()
        .readLines()
        .joinToString("\n") { it }

    schemaSource.isNotEmpty() eq true

    loader = jvmCompileAndLoad(
        schema = schemaSource,
        packageName = "com.yelp"
    )
  }

  @Test fun `yelp graphql schema compiles to ktq jvm objects`() =
      loader notEq null

  /** this reflection testing setup is the is the hottest thing I've ever written */
  @Test fun `yelp graphql "Business" fields match ktq type`() = loader.loadClass("com.yelp.Business") {

    kprop("name") { nameProp ->
      nameProp requireReturns StringDelegate.Query::class
    }

    kprop("is_claimed") {
      it requireReturns BooleanDelegate.Query::class
    }

    kprop("review_count") { intField ->
      intField requireReturns IntDelegate.Query::class
    }

    kprop("rating") { floatProp ->
      floatProp requireReturns FloatDelegate.Query::class
    }

  }.ignore()
}