package com.micabytes.ink

import com.micabytes.ink.InkParser.parseDivert
import java.math.BigDecimal
import java.util.*

internal class Choice(textBase: String,
                      val level: Int,
                      parent: Container,
                      lineNumber: Int) : Container(Choice.getId(textBase, parent), Choice.extractChoiceText(textBase), parent, lineNumber) {
  private var conditions: MutableList<String> = ArrayList()
  private val repeatable = (textBase[0] == InkParser.CHOICE_PLUS)

  init {
    val notation = textBase[0]
    var header = textBase
    while (header[0] == notation)
      header = header.substring(1).trim({ it <= ' ' })
    if (header.startsWith(StoryText.BRACE_LEFT)) {
      header = header.substring(header.indexOf(StoryText.BRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    while (header.startsWith(StoryText.CBRACE_LEFT)) {
      conditions.add(header.substring(header.indexOf(StoryText.CBRACE_LEFT) + 1, header.indexOf(StoryText.CBRACE_RIGHT)).trim({ it <= ' ' }))
      header = header.substring(header.indexOf(StoryText.CBRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    val result = if (header.contains("]"))
      header.replace("\\[.*\\]".toRegex(), "").trim({ it <= ' ' })
    else
      header
    if (!result.isEmpty()) {
      if (result.contains(InkParser.DIVERT))
        parseDivert(lineNumber, result, this)
      else
        children.add(Content(id + InkParser.DOT + size, result, this, lineNumber))
    }
  }

  @Throws(InkRunTimeException::class)
  override fun getText(story: Story): String {
    return StoryText.getText(text, count, story)
  }

  fun isFallBack() : Boolean = text.isEmpty()

  fun evaluateConditions(story: Story): Boolean {
    if (count > 0 && !repeatable)
      return false
    for (condition in conditions) {
      try {
        val obj = Declaration.evaluate(condition, story)
        if (obj is Boolean && !obj)
          return false
        if (obj is BigDecimal && obj.toInt() <= 0)
          return false
      } catch (e: InkRunTimeException) {
        story.logException(e)
        return false
      }
    }
    return true
  }

  companion object {

    fun getChoiceDepth(line: String) : Int {
      val notation = line[0]
      var lvl = 0
      var s = line.substring(1).trim({ it <= ' ' })
      while (s[0] == notation) {
        lvl++
        s = s.substring(1).trim({ it <= ' ' })
      }
      return lvl
    }

    fun getId(header: String, parent: Container): String {
      val notation = header[0]
      var id = header
      while (id.startsWith(notation))
        id = id.substring(1).trim({ it <= ' ' })
      if (id.startsWith(StoryText.BRACE_LEFT)) {
        id = id.substring(id.indexOf(StoryText.BRACE_LEFT) + 1, id.indexOf(StoryText.BRACE_RIGHT)).trim({ it <= ' ' })
        return parent.id + InkParser.DOT + id
      }
      return parent.id + InkParser.DOT + parent.size
    }

    fun extractChoiceText(header: String): String {
      val notation = header[0]
      var text = header
      while (text[0] == notation)
        text = text.substring(1).trim({ it <= ' ' })
      if (text.startsWith(StoryText.BRACE_LEFT)) {
        text = text.substring(text.indexOf(StoryText.BRACE_RIGHT) + 1).trim({ it <= ' ' })
      }
      while (text.startsWith(StoryText.CBRACE_LEFT)) {
        text = text.substring(text.indexOf(StoryText.CBRACE_RIGHT) + 1).trim({ it <= ' ' })
      }
      if (text.contains("]"))
        text = text.substring(0, text.indexOf(StoryText.SBRACE_RIGHT)).replace(StoryText.SBRACE_LEFT, "").trim({ it <= ' ' })
      return text
    }

    fun getParent(currentContainer: Container, lvl: Int) : Container {
      var ret = currentContainer
      val parentFound = false
      while (!parentFound) {
        if ((ret is Choice && lvl <= ret.level) || (ret is Gather && lvl <= ret.level))
          ret = ret.parent!!
        else
          return ret
      }
      return ret
    }

  }

}
