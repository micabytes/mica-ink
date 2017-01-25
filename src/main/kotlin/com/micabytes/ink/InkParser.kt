package com.micabytes.ink

import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

object InkParser {
  private val WHITESPACE = ' '
  internal val HEADER = '='
  internal val DASH = '-'
  internal val CHOICE_DOT = '*'
  internal val CHOICE_PLUS = '+'
  internal val DIVERT = "->"
  private val VAR_DECL = 'V'
  private val VAR_STAT = '~'
  val CONDITIONAL_HEADER = '{'
  internal val CONDITIONAL_END = '}'
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
    val content = HashMap<String, Content>()
    var topContainer: Container? = null
    inputStream.reader(Charsets.UTF_8).buffered(DEFAULT_BUFFER_SIZE).use {
      var line: String? = it.readLine()
      var lineNumber = 1
      var currentContainer: Container? = null
      while (line != null) {
        var trimmedLine = line.trim { it <= ' ' }
        if (trimmedLine.contains("//")) {
          val comment = trimmedLine.substring(trimmedLine.indexOf("//")).trim({ it <= ' ' })
          parseComment(lineNumber, comment, currentContainer)
          trimmedLine = trimmedLine.substring(0, trimmedLine.indexOf("//")).trim({ it <= ' ' })
        }
        if (trimmedLine.startsWith(INCLUDE)) {
          // TODO: Include
          //val includeFile = trimmedLine.replace(INCLUDE, "").trim({ it <= ' ' })
          //val incl = parse(provider, includeFile)
          //story.addAll(incl)
        }
        val tokens = parseLine(lineNumber, trimmedLine, currentContainer)
        for (current in tokens) {
          if (current is Container)
            currentContainer = current
          if (!content.containsKey(current.id))
            content.put(current.id, current)
          if (currentContainer != null && topContainer == null)
            topContainer = currentContainer
        }
        line = it.readLine()
        lineNumber++
      }
    }
    if (topContainer == null) throw InkParseException("Could not detect a root knot node in " + fileName)
    return Story(provider, fileName, topContainer!!, content)
  }

  @Throws(InkParseException::class)
  internal fun parseLine(lineNumber: Int, line: String, currentContainer: Container?): List<Content> {
    val firstChar = if (line.isEmpty()) WHITESPACE else line[0]
    when (firstChar) {
      HEADER -> {
        if (Knot.isKnot(line)) {
          return mutableListOf(Knot(line, lineNumber))
        }
        if (Stitch.isStitchHeader(line)) {
          if (currentContainer == null)
            throw InkParseException("Stitch without a containing Knot at line " + lineNumber) // add fileName
          return parseContainer(Stitch(line, Stitch.getParent(currentContainer), lineNumber))
        }
      }
      CHOICE_DOT, CHOICE_PLUS -> {
        if (currentContainer == null)
          throw InkParseException("Choice without an anchor at line " + lineNumber) // add fileName
        val choiceDepth = Choice.getChoiceDepth(line)
        return parseContainer(Choice(line, choiceDepth, Choice.getParent(currentContainer, choiceDepth), lineNumber))
      }
      DASH -> {
        if (line.startsWith(DIVERT))
          return parseDivert(lineNumber, line, currentContainer)
        if (currentContainer == null)
          throw InkParseException("Dash without an anchor at line " + lineNumber) // add fileName
        if (isConditional(currentContainer)) {
          return parseContainer(ConditionalOption(line, getConditional(currentContainer), lineNumber))
        }
        else {
          val level = Gather.getChoiceDepth(line)
          return parseContainer(Gather(line, Gather.getParent(currentContainer, level), level, lineNumber))
        }
      }
      VAR_DECL, VAR_STAT -> {
        if (currentContainer == null)
          throw InkParseException("Declaration is not inside a knot/container at line " + lineNumber) // add fileName
        if (Declaration.isVariableHeader(line)) {
          return parseContainer(Declaration(lineNumber, line, currentContainer))
        }
      }
      CONDITIONAL_HEADER -> if (Conditional.isConditionalHeader(line))
        return parseContainer(Conditional(line, currentContainer!!, lineNumber))
      CONDITIONAL_END -> if (currentContainer is Conditional || currentContainer is ConditionalOption)
        return mutableListOf(getConditional(currentContainer).parent as Content)
      else -> {
        // NOOP
      }
    }
    if (line.contains(DIVERT))
      return parseDivert(lineNumber, line, currentContainer)
    if (!line.isEmpty() && currentContainer != null)
      return parseContainer(Content(Content.getId(currentContainer), line, currentContainer, lineNumber))
    // Should throw error.
    return ArrayList<Content>()
  }

  private fun parseContainer(cont: Content): MutableList<Content> {
    cont.parent!!.add(cont)
    val list = mutableListOf(cont)
    if (cont is Container)
      list.addAll(cont.children)
    return list
  }

  fun isConditional(currentContainer: Container?): Boolean {
    var container = currentContainer
    while (container != null) {
      if (container is Conditional) return true
      container = container.parent
    }
    return false
  }

  fun getConditional(currentContainer: Container): Container {
    var container = currentContainer
    while (container != null) {
      if (container is Conditional) return container
      container = container.parent!!
    }
    return currentContainer
  }

  internal fun parseDivert(lineNumber: Int, line: String, currentContainer: Container?): List<Content> {
    val ret = ArrayList<Content>()
    val div = line.split(DIVERT)
    if (!div[0].isEmpty()) {
      val text = Content(Content.getId(currentContainer!!), div[0] + Symbol.GLUE, currentContainer, lineNumber)
      currentContainer.add(text)
      ret.add(text)
    }
    for (i in 1 until div.size) {
      if (div[i].isNotEmpty()) {
        val divert = Divert(lineNumber, div[i], currentContainer!!)
        currentContainer.add(divert)
        ret.add(divert)
      }
    }
    return ret
  }

  private fun parseComment(lineNumber: Int, comment: String, current: Container?) {
    /*val token: Array<String> = AT_SPLITTER.split(comment)
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
    }*/
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
/*
else if (conditional != null) {
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
*/
