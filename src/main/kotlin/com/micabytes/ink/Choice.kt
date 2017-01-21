package com.micabytes.ink

import com.micabytes.ink.InkParser.parseDivert
import java.math.BigDecimal
import java.util.*

internal class Choice(lineNumber: Int,
                      textBase: String,
                      parent: Container,
                      val level : Int) : Container(lineNumber, textBase, parent) {
  override var id: String = if (parent != null) parent.id + InkParser.DOT + parent.indexOf(this) else ""
  override var text: String = textBase
  private var conditions: ArrayList<String> = ArrayList()
  private val repeatable = (text[0] == InkParser.CHOICE_PLUS)

  init {
    val notation = text[0]
    text = text.substring(1).trim({ it <= ' ' })
    while (text.get(0) == notation)
      text = text.substring(1).trim({ it <= ' ' })
    if (text.startsWith(StoryText.BRACE_LEFT)) {
      id = text.substring(text.indexOf(StoryText.BRACE_LEFT) + 1, text.indexOf(StoryText.BRACE_RIGHT)).trim({ it <= ' ' })
      val p = parent
      id = p!!.id + InkParser.DOT + id
      text = text.substring(text.indexOf(StoryText.BRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    while (text.startsWith(StoryText.CBRACE_LEFT)) {
      val c = text.substring(text.indexOf(StoryText.CBRACE_LEFT) + 1, text.indexOf(StoryText.CBRACE_RIGHT)).trim({ it <= ' ' })
      conditions.add(c)
      text = text.substring(text.indexOf(StoryText.CBRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    val result = (if (text.contains("]"))
      text.replace("\\[.*\\]".toRegex(), "").trim({ it <= ' ' })
    else
      text.trim({ it <= ' ' })).trimStart(InkParser.CHOICE_DOT, InkParser.CHOICE_PLUS, ' ')
    if (!result.isEmpty()) {
      if (result.contains(InkParser.DIVERT))
        parseDivert(lineNumber, result, this)
      else
        Content(lineNumber, result, this)
    }
    if (text.contains("]"))
      text = text.substring(0, text.indexOf(StoryText.SBRACE_RIGHT)).replace(StoryText.SBRACE_LEFT, "").trim({ it <= ' ' })
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

    fun isChoiceHeader(str: String): Boolean {
      if (str.length < 2) return false
      return str[0] == InkParser.CHOICE_DOT || str[0] == InkParser.CHOICE_PLUS
    }

    fun getChoiceDepth(line: String) : Int {
      val notation = line[0]
      var lvl = 0
      var s = line.substring(1).trim({ it <= ' ' })
      while (s.get(0) == notation) {
        lvl++
        s = s.substring(1).trim({ it <= ' ' })
      }
      return lvl
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
