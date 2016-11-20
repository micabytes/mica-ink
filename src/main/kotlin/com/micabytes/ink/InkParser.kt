package com.micabytes.ink

import java.io.InputStream
import java.util.regex.Pattern

object InkParser {
  private val WHITESPACE = ' '
  internal val HEADER = '='
  private val GATHER_DASH = '-'
  internal val CHOICE_DOT = '*'
  internal val CHOICE_PLUS = '+'
  private val VAR_DECL = 'V'
  private val VAR_STAT = '~'
  private val CONDITIONAL_HEADER = '{'
  internal val CONDITIONAL_END = "}"
  val DOT = '.'
  private val DEFAULT_KNOT_NAME = "default"
  private val AT_SPLITTER = Pattern.compile("[@]")
  private val INCLUDE = "INCLUDE"
  private val IMG = "img("

  @Throws(InkParseException::class)
  fun parse(provider: StoryWrapper, fileName: String): Story {
    val input: InputStream = provider.getStream(fileName) ?: throw InkParseException("Could get InputStream from filename " + fileName)
    return parse(input, provider, fileName)
  }

  @Throws(InkParseException::class)
  fun parse(inputStream: InputStream, provider: StoryWrapper, fileName: String): Story {
    val story = Story(provider, fileName)
    inputStream.reader(Charsets.UTF_8).buffered(DEFAULT_BUFFER_SIZE).use {
      var line: String? = it.readLine()
      var lineNumber = 1
      var current: Container = Knot(lineNumber, "=== " + DEFAULT_KNOT_NAME)
      story.add(current)
      var conditional: Conditional? = null
      while (line != null) {
        var trimmedLine = line.trim { it <= ' ' }
        if (trimmedLine.contains("//")) {
          val comment = trimmedLine.substring(trimmedLine.indexOf("//")).trim({ it <= ' ' })
          parseComment(lineNumber, comment, current)
          trimmedLine = trimmedLine.substring(0, trimmedLine.indexOf("//")).trim({ it <= ' ' })
        }
        if (trimmedLine.startsWith(INCLUDE)) {
          val includeFile = trimmedLine.replace(INCLUDE, "").trim({ it <= ' ' })
          val incl = parse(provider, includeFile)
          story.addAll(incl)
        } else if (conditional != null) {
          val cond = current.get(current.size - 1) as Conditional
          //cond.parseLine(lineNumber, trimmedLine)
          if (trimmedLine.endsWith(CONDITIONAL_END))
          // This is a bug. Means conditions cannot have text line that ends with CBRACE_RIGHT
            conditional = null
        } else {
          val cont = parseLine(lineNumber, trimmedLine, current)
          if (cont != null) {
            //cont.generateId(current)
            story.add(cont)
            if (cont is Container) {
              current = cont as Container
            }
          }
          if (cont != null && cont is Conditional) {
            conditional = cont as Conditional?
          }
        }
        line = it.readLine()
        lineNumber++
      }
      story.initialize()
    }
    return story
  }

  @SuppressWarnings("OverlyComplexMethod")
  @Throws(InkParseException::class)
  internal fun parseLine(lineNumber: Int, trimmedLine: String, current: Container): Content? {
    val firstChar = if (trimmedLine.isEmpty()) WHITESPACE else trimmedLine[0]
    when (firstChar) {
      HEADER -> {
        //if (KnotFunction.isFunctionHeader(trimmedLine)) {
        //  return KnotFunction(lineNumber, trimmedLine)
        //}
        if (Knot.isKnotHeader(trimmedLine)) {
          return Knot(lineNumber, trimmedLine)
        }
        //if (Stitch.isStitchHeader(trimmedLine)) {
        //  return Stitch(lineNumber, trimmedLine, current)
        //}
      }
      CHOICE_DOT, CHOICE_PLUS -> if (Choice.isChoiceHeader(trimmedLine))
        return Choice(lineNumber, trimmedLine, current)
      //GATHER_DASH -> if (Gather.isGatherHeader(trimmedLine))
      //  return Gather(lineNumber, trimmedLine, current)
      //VAR_DECL, VAR_STAT -> if (Variable.isVariableHeader(trimmedLine))
      //  return Variable(lineNumber, trimmedLine, current)
      //CONDITIONAL_HEADER -> if (Conditional.isConditionalHeader(trimmedLine))
      //  return Conditional(lineNumber, trimmedLine, current)
      else -> {
      }
    }
    if (!trimmedLine.isEmpty()) {
      return Content(lineNumber, trimmedLine, current)
    }
    return null
  }

  private fun parseComment(lineNumber: Int, comment: String, current: Container?) {
    val token: Array<String> = AT_SPLITTER.split(comment)
    if (token.size < 2) return
    for (i in 1..token.size - 1) {
      if (token[i].startsWith(IMG)) {
        val img = token[i].substring(token[i].indexOf(Symbol.BRACE_LEFT) + 1, token[i].indexOf(Symbol.BRACE_RIGHT)).trim({ it <= ' ' })
        if (current != null) {
          current.background = img
        }
      }
      //if (token[i].startsWith(CHOICE_DOT.toString()) || token[i].startsWith(CHOICE_PLUS.toString())) {
      //  val cont = Comment(lineNumber, token[i])
      //  current?.add(cont)
      //}
    }
  }

}



/*

catch (e: IOException) {
  provider.logException(e)
} finally {
  try {
    if (inputStreamReader != null)
      inputStreamReader.close()
  } catch (e: IOException) {
    provider.logException(e)
  }

  try {
    if (bufferedReader != null)
      bufferedReader.close()
  } catch (e: IOException) {
    provider.logException(e)
  }
}
*/
