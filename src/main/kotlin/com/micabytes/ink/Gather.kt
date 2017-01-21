package com.micabytes.ink

internal class Gather @Throws(InkParseException::class)
constructor(lineNumber: Int,
            text: String,
            parent: Container,
            val level : Int) : Container(lineNumber, text, parent) {
  override var id: String = parent.id + InkParser.DOT + parent.indexOf(this)

  init {
    var str = text.substring(1).trim({ it <= ' ' })
    while (str.get(0) == InkParser.DASH)
      str = str.substring(1).trim({ it <= ' ' })
    if (str.startsWith(StoryText.BRACE_LEFT)) {
      id = str.substring(str.indexOf(StoryText.BRACE_LEFT) + 1, str.indexOf(StoryText.BRACE_RIGHT)).trim({ it <= ' ' })
      id = parent.id + InkParser.DOT + id
      str = str.substring(str.indexOf(StoryText.BRACE_RIGHT) + 1).trim({ it <= ' ' })
    }
    if (!str.isEmpty()) {
      if (str.contains(InkParser.DIVERT))
        InkParser.parseDivert(lineNumber, str, this)
      else
        Content(lineNumber, str, this)
    }
  }

  companion object {

    fun getChoiceDepth(line: String): Int {
      val notation = line[0]
      var lvl = 0
      var s = line.substring(1).trim({ it <= ' ' })
      while (s.get(0) == notation) {
        lvl++
        s = s.substring(1).trim({ it <= ' ' })
      }
      return lvl
    }

    fun getParent(currentContainer: Container, lvl: Int): Container {
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
