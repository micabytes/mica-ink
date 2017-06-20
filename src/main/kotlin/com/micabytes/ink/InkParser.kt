package com.micabytes.ink

import com.micabytes.ink.exception.InkParseException
import java.io.InputStream
import java.util.*

object InkParser {

  @Throws(InkParseException::class)
  fun parse(provider: StoryWrapper, fileName: String): Story {
    val input: InputStream = provider.getStream(fileName)
    return parse(input, provider, fileName)
  }

  @Throws(InkParseException::class)
  fun parse(inputStream: InputStream, provider: StoryWrapper, fileName: String): Story {
    val includes = ArrayList<String>()
    val content = HashMap<String, Content>()
    var topContainer: Container? = null
    inputStream.reader(Charsets.UTF_8).buffered(DEFAULT_BUFFER_SIZE).use {
      var line: String? = it.readLine()
      var lineNumber = 1
      var currentContainer: Container? = null
      while (line != null) {
        var trimmedLine = line.trim { it <= ' ' }
        if (trimmedLine.contains(Symbol.COMMENT)) {
          trimmedLine = trimmedLine.substring(0, trimmedLine.indexOf(Symbol.COMMENT)).trim({ it <= ' ' })
        }
        // Tags don't seem to work-
        if (trimmedLine.contains(Symbol.HASHMARK)) {
          val tags = trimmedLine.substring(trimmedLine.indexOf(Symbol.HASHMARK)).trim({ it <= ' ' }).split(Symbol.HASHMARK)
          for (tag in tags) {
            val current = Tag(tag, currentContainer, lineNumber)
            if (currentContainer != null)
              currentContainer.add(current)
            if (!content.containsKey(current.id))
              content.put(current.id, current)
          }
          trimmedLine = trimmedLine.substring(0, trimmedLine.indexOf(Symbol.HASHMARK)).trim({ it <= ' ' })
        }
        if (trimmedLine.startsWith(Symbol.INCLUDE)) {
          val includeFile = trimmedLine.replace(Symbol.INCLUDE, "").trim({ it <= ' ' })
          includes.add(includeFile)
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
    val story = Story(provider, fileName, topContainer!!, content)
    for (includeFile in includes) {
      story.add(parse(provider, includeFile))
    }
    return story
  }

  @Throws(InkParseException::class)
  internal fun parseLine(lineNumber: Int, line: String, currentContainer: Container?): List<Content> {
    val firstChar = if (line.isEmpty()) Symbol.WHITESPACE else line[0]
    when (firstChar) {
      Symbol.HEADER -> {
        if (Knot.isKnot(line)) {
          return mutableListOf(Knot(line, lineNumber))
        }
        if (Stitch.isStitchHeader(line)) {
          if (currentContainer == null)
            throw InkParseException("Stitch without a containing Knot at line " + lineNumber) // add fileName
          return parseContainer(Stitch(line, Stitch.getParent(currentContainer), lineNumber))
        }
      }
      Symbol.CHOICE_DOT, Symbol.CHOICE_PLUS -> {
        if (currentContainer == null)
          throw InkParseException("Choice without an anchor at line " + lineNumber) // add fileName
        val choiceDepth = Choice.getChoiceDepth(line)
        return parseContainer(Choice(line, choiceDepth, Choice.getParent(currentContainer, choiceDepth), lineNumber))
      }
      Symbol.DASH -> {
        if (line.startsWith(Symbol.DIVERT))
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
      Symbol.VAR_DECL, Symbol.VAR_STAT -> {
        if (currentContainer == null)
          throw InkParseException("Declaration is not inside a knot/container at line " + lineNumber) // add fileName
        if (Declaration.isVariableHeader(line)) {
          return parseContainer(Declaration(lineNumber, line, currentContainer))
        }
      }
      Symbol.CBRACE_LEFT -> if (Conditional.isConditionalHeader(line))
        return parseContainer(Conditional(line, currentContainer!!, lineNumber))
      Symbol.CBRACE_RIGHT -> if (currentContainer is Conditional || currentContainer is ConditionalOption)
        return mutableListOf(getConditional(currentContainer).parent as Content)
      else -> {
        // NOOP
      }
    }
    if (line.contains(Symbol.DIVERT))
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
    val div = line.split(Symbol.DIVERT)
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

}
