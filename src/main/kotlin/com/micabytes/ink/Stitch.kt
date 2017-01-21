package com.micabytes.ink

import java.util.ArrayList

internal class Stitch(lineNumber: Int,
                    text: String) : ParameterizedContainer(lineNumber, text, null) {
  override var id: String = ""
  val level: Int = 0
  internal var isFunction: Boolean = false

  init {
    var pos = 0
    while (InkParser.HEADER == text[pos]) {
      pos++
    }
    val header = StringBuilder(pos + 1)
    for (i in 0..pos - 1)
      header.append(InkParser.HEADER)
    var fullId = text.replace(header.toString().toRegex(), "").trim({ it <= ' ' })
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
    fun isStitchHeader(str: String): Boolean {
      return str.startsWith(KNOT_HEADER)
    }
  }

}

/*
internal class Stitch

@Throws(InkParseException::class)
constructor(lineNumber: Int,
            content: String,
            parent: Container?) : Content(lineNumber, content, parent) {

    init {
        lineNumber = l
        level = 1
        if (current == null)
            throw InkParseException("A stitch cannot be defined without a parent Knot")
        parent = current.getParent(0)
        parent!!.add(this)
        type = ContentType.STITCH
        var fullId = StringBuilder(parent!!.id).append(InkParser.DOT).append(str.replace(InkParser.HEADER.toString().toRegex(), "").trim({ it <= ' ' })).toString()
        if (fullId.contains(StoryText.BRACE_LEFT)) {
            val params = fullId.substring(fullId.indexOf(StoryText.BRACE_LEFT) + 1, fullId.indexOf(StoryText.BRACE_RIGHT))
            val param = params.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            parameters = ArrayList<String>()
            for (aParam in param) getParameters().add(aParam.trim({ it <= ' ' }))
            fullId = fullId.substring(0, fullId.indexOf(StoryText.BRACE_LEFT))
        }
        //if (fullId.startsWith(FUNCTION)) {
        //
        //}
        id = fullId
    }

    companion object {

        fun isStitchHeader(str: String): Boolean {
            if (str.length < 2) return false
            return str[0] == InkParser.HEADER && str[1] != InkParser.HEADER
        }
    }

}
*/