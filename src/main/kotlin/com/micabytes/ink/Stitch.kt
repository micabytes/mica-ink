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
      if (id.contains(Symbol.BRACE_LEFT)) {
        id = id.substring(0, id.indexOf(Symbol.BRACE_LEFT))
      }
      return parent.id + Symbol.DOT + id
    }

    fun getParent(currentContainer: Container): Container {
      var container = currentContainer
      while (container != null) {
        if (container is Knot) return container
        container = container.parent!!
      }
      return currentContainer
    }

  }

}
