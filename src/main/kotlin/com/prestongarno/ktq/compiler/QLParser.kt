package com.prestongarno.ktq.compiler

import com.prestongarno.ktq.compiler.qlang.spec.QCustomScalarType
import com.prestongarno.ktq.compiler.qlang.spec.QDirectiveSymbol
import com.prestongarno.ktq.compiler.qlang.spec.QEnumDef
import com.prestongarno.ktq.compiler.qlang.spec.QField
import com.prestongarno.ktq.compiler.qlang.spec.QFieldInputArg
import com.prestongarno.ktq.compiler.qlang.spec.QInputType
import com.prestongarno.ktq.compiler.qlang.spec.QInterfaceDef
import com.prestongarno.ktq.compiler.qlang.spec.QSchemaType
import com.prestongarno.ktq.compiler.qlang.spec.QTypeDef
import com.prestongarno.ktq.compiler.qlang.spec.QUnionTypeDef
import com.prestongarno.ktq.compiler.qlang.spec.QUnknownInterface
import com.prestongarno.ktq.compiler.qlang.spec.QUnknownType
import com.prestongarno.ktq.compiler.qlang.spec.RootType
import java.io.File
import java.util.Scanner
import java.io.InputStream

/**
 * Created by preston on 7/20/17.
 */

object QLParser {

  fun parse(file: File): QCompilationUnit = parse(file.inputStream())

  fun parse(value: String): QCompilationUnit = parse(value.byteInputStream())

  fun parse(ioStream: InputStream): QCompilationUnit {

    val all = mutableListOf<QSchemaType<*>>()

    val scanner = Scanner(ioStream)

    var comments = ""

    while (scanner.hasNext()) {
      val declType = scanner.useDelimiter("\\s").next().trim()

      comments =
          if (comments.isNotEmpty()) {
            comments
          } else if (declType.trim().startsWith("#")) {
            var entire = declType.append(scanner.nextLine())
            while (scanner.hasNext("\\s*#.*"))
              entire = entire.append("<<||>>" + scanner.nextLine().trim())
            comments = entire.trim()
                .substring(1)
                .replace("<<||>>#", "\n")
            continue
          } else ""

      val typeKind = RootType.match(declType)

      val name = scanner.next().trim()

      when (typeKind) {
        RootType.UNKNOWN -> {
          throw IllegalArgumentException("Unknown type declaration \"$declType\"")
        }

        RootType.UNION -> {
          scanner.useDelimiter("[a-zA-Z0-9_]".toRegex().pattern).next()
          val block = scanner.nextLine()
          all.add(0, QUnionTypeDef(name, QLexer.unionFields(block)
              .map { str -> QUnknownType(str) }))
        }

        RootType.TYPE -> {
          val ifaces = scanner.useDelimiter("\\{")
              .next()
              .split("[\\s,]".toRegex())
              .filter { s -> s.isNotBlank() }

          val fields = lexFieldsToSymbols(QLexer.baseFields(scanner.getClosure()))
          all.add(0, QTypeDef(name,
              if (ifaces.isEmpty()) {
                emptyList()
              } else {
                ifaces.subList(1, ifaces.size)
                    .map { s -> QUnknownInterface(s) }
              }
              , fields))
        }

        RootType.INTERFACE -> all.add(0, QInterfaceDef(name,
            lexFieldsToSymbols(QLexer.baseFields(scanner.getClosure()))
                .also {
                  it.forEach { it.abstract(true) }
                }))

        RootType.SCALAR -> all.add(0, QCustomScalarType(name))
        RootType.INPUT -> all.add(0, QInputType(name, lexFieldsToSymbols(QLexer.baseFields(scanner.getClosure()))))
        RootType.ENUM -> {
          val element = QEnumDef(name, QLexer.enumFields(scanner.useDelimiter("}").next()))
          all.add(0, element)
        }
      }
      if (comments.trim().isNotEmpty()) {
        all[0].description = comments
        comments = ""
      }
      scanner.useDelimiter("[a-zA-Z#]").next()
    }

    return QCompilationUnit(all)
  }

  private fun lexFieldsToSymbols(fields: List<Field>): List<QField> =
      fields.map { (symbol, inputArgs, type, directive, isList, isNullable, comment) ->
        QField(symbol, QUnknownType(type),
            inputArgs.map { arg ->
              QFieldInputArg(arg.symbol,
                  QUnknownType(arg.type),
                  arg.defaultValue,
                  arg.isList,
                  arg.isNullable)
            },
            QDirectiveSymbol(QUnknownType(directive.first), directive.second),
            isList,
            isNullable,
            comment)
      }
}

private fun Scanner.getClosure() = useDelimiter("}").next().trim().substring(1)

private fun String.append(to: String) = this + to
