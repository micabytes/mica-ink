package com.micabytes.ink

import com.micabytes.ink.InkParser.parseDivert
import java.math.BigDecimal
import java.util.ArrayList

internal class Choice(lineNumber: Int,
                      text: String,
                      parent: Container,
                      val level : Int) : Container(lineNumber, text, parent) {
  private var conditions: ArrayList<String> = ArrayList()
  private val repeatable = (text[0] == InkParser.CHOICE_PLUS)

  init {
    val notation = text[0]
    var s = text.substring(1).trim({ it <= ' ' })
    while (s.get(0) == notation)
      s = s.substring(1).trim({ it <= ' ' })
    parent.let { add(this) }
    addLine(s)
  }

  private fun addLine(str: String) {
    var s = str.trim { it <= ' ' }
    /*
    if (s.startsWith("(")) {
      id = s.substring(s.indexOf(StoryText.BRACE_LEFT) + 1, s.indexOf(StoryText.BRACE_RIGHT)).trim({ it <= ' ' })
      val p = parent
      assert(p != null)
      id = p!!.id + InkParser.DOT + id
      s = s.substring(s.indexOf(StoryText.BRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    */
    if (s.startsWith(StoryText.CBRACE_LEFT) && conditions == null)
      conditions = ArrayList<String>()
    while (s.startsWith("{")) {
      val c = s.substring(s.indexOf(StoryText.CBRACE_LEFT) + 1, s.indexOf(StoryText.CBRACE_RIGHT)).trim({ it <= ' ' })
      conditions.add(c)
      s = s.substring(s.indexOf(StoryText.CBRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    // TODO: text = getChoiceText(s)
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
  }

  @Throws(InkRunTimeException::class)
  override fun getText(story: Story): String {
    return StoryText.getText(
        if (text.contains("]"))
          text.substring(0, text.indexOf(StoryText.SBRACE_RIGHT)).replace(StoryText.SBRACE_LEFT, "").trim({ it <= ' ' })
        else
          text,
        count,
        story).trimStart(InkParser.CHOICE_DOT, InkParser.CHOICE_PLUS, ' ')
  }

  @Throws(InkRunTimeException::class)
  fun evaluateConditions(story: Story): Boolean {
    if (count > 0 && type == ContentType.CHOICE_ONCE)
      return false
    if (conditions == null)
      return true
    for (condition in conditions!!) {
      try {
        val obj = Variable.evaluate(condition, story) ?: return false
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
      if (currentContainer is Choice && lvl <= currentContainer.level)
        return currentContainer.parent!!
      return currentContainer
    }
  }

}
