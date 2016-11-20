package com.micabytes.ink

// TODO: Functions should check that stitches are not added.

internal class Knot(lineNumber: Int,
                    content: String) : ParameterizedContainer(lineNumber, content, null) {
  override var id: String = ""
  override val level: Int = 0
  internal var isFunction: Boolean = false

  init {
    var pos = 0
    while (InkParser.HEADER == content[pos]) {
      pos++
    }
    val header = StringBuilder(pos + 1)
    for (i in 0..pos - 1)
      header.append(InkParser.HEADER)
    var fullId = content.replace(header.toString().toRegex(), "").trim({ it <= ' ' })
    if (fullId.startsWith(Symbol.FUNCTION)) {
      isFunction = true
      fullId = fullId.replaceFirst(Symbol.FUNCTION.toRegex(), "")
    }
    if (fullId.contains(StoryText.BRACE_LEFT)) {
      val params = fullId.substring(fullId.indexOf(StoryText.BRACE_LEFT) + 1, fullId.indexOf(StoryText.BRACE_RIGHT))
      val param = params.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      param.mapTo(parameters) { aParam -> aParam.trim({ it <= ' ' }) }
      fullId = fullId.substring(0, fullId.indexOf(StoryText.BRACE_LEFT))
    }
    id = fullId
  }

  companion object {
    private val KNOT_HEADER = "=="

    fun isKnotHeader(str: String): Boolean {
      return str.startsWith(KNOT_HEADER)
    }
  }

}
