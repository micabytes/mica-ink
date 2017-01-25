package com.micabytes.ink

internal class Stitch(header: String,
                      parent: Container,
                      lineNumber: Int) : ParameterizedContainer(getId(header, parent), ParameterizedContainer.getParameters(header), parent, lineNumber) {

  companion object {
    private val STITCH_HEADER = "="

    fun isStitchHeader(str: String): Boolean {
      return str.startsWith(STITCH_HEADER)
    }

    fun  getId(header: String, parent: Container): String {
      var id = header.replace(STITCH_HEADER.toRegex(), "").trim({ it <= ' ' })
      if (id.startsWith(Symbol.FUNCTION)) {
        id = id.replaceFirst(Symbol.FUNCTION.toRegex(), "")
      }
      if (id.contains(StoryText.BRACE_LEFT)) {
        id = id.substring(0, id.indexOf(StoryText.BRACE_LEFT))
      }
      return parent.id + InkParser.DOT + id
    }

    fun getParent(currentContainer: Container): Container {
      var container = currentContainer
      while (container != null) {
        if (container is Knot) return container
        container = container.parent!!
      }
      return currentContainer!!
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