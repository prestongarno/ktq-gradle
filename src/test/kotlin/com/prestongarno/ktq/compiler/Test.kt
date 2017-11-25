package com.prestongarno.ktq.compiler

import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.time.Instant
import java.util.*
import kotlin.reflect.KCallable

/** F# expression redirect for single exp. return types */
fun Any?.ignore() = Unit

@Suppress("NOTHING_TO_INLINE") inline infix fun Any?.eq(other: Any?) = assertThat(this).isEqualTo(other)
@Suppress("NOTHING_TO_INLINE") inline infix fun Any?.notEq(other: Any?) = assertThat(this).isNotEqualTo(other)

inline fun <reified T> assertThrows(block: () -> Unit): ThrowableSubject {
  try {
    block()
  } catch (e: Throwable) {
    if (e is T) {
      return assertThat(e)
    } else {
      throw e
    }
  }
  throw AssertionError("Expected ${T::class.simpleName}")
}

fun compileGraphQl(schema: String, block: (GraphQLCompiler.() -> Unit)? = null) =
    GraphQLCompiler(StringSchema(schema)).apply {
      compile()
      block?.invoke(this)
    }.definitions

fun compileOut(schema: String, includeImports: Boolean = true, block: (GraphQLCompiler.() -> Unit)? = null): String =
    compileGraphQl(schema, block).toFileSpec().let {
      val target = StringBuilder()
      it.writeTo(target)
      target.toString() // smh kotlinpoet
          .let {
            if (includeImports) it else
              it.replace("^import.*\n".toRegex(RegexOption.MULTILINE), "")
                  .replace("^package.*\n".toRegex(RegexOption.MULTILINE), "")
                  .replace("^\n\n", "")
          }
    }

fun Set<SchemaType<*>>.toFileSpec(baseName: String = "GraphQLSchema"): FileSpec =
    FileSpec.builder("", "$baseName${Instant.now().toEpochMilli()}.kt").apply {
      map(SchemaType<*>::toKotlin).let(this::addTypes)
    }.build()

// Not a test case without a functional println
fun Any?.println() = println(this)

private fun FileSpec.Builder.addTypes(types: Iterable<TypeSpec>) = types.forEach { addType(it) }

fun <T> emptyBlock(): T.() -> Unit = Block.empty<T>()

private object Block {

  private val value: Any.() -> Unit = { /* Nothing */ }

  @Suppress("UNCHECKED_CAST") fun <T> empty(): T.() -> Unit = value as T.() -> Unit
}
