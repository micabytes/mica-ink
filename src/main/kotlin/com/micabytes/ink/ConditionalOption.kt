package com.micabytes.ink

import java.math.BigDecimal

class ConditionalOption internal constructor(condition: String,
                                             parent: Container,
                                             lineNumber: Int) : Container(getId(parent), getCondition(condition), parent, lineNumber) {

  init {
    if (condition.startsWith(Symbol.DASH) && !condition.endsWith(':')) {
      val txt = condition.substring(1).trim({ it <= ' ' })
      if (!txt.isEmpty()) {
        if (txt.contains(InkParser.DIVERT))
          InkParser.parseDivert(lineNumber, txt, this)
        else
          children.add(Content(id + InkParser.DOT + size, txt, this, lineNumber))
      }
    }
  }

  fun  evaluate(vMap: VariableMap): Boolean {
    val res = Expression(text).eval(vMap)
    return (res as BigDecimal).toInt() > 0
  }

  companion object {

    fun getCondition(txt : String): String {
      if (txt.isEmpty()) return txt
      if (!txt.endsWith(":"))
        return ""
      var str = if (txt.startsWith(Symbol.DASH) || txt.startsWith(InkParser.CONDITIONAL_HEADER))
        txt.substring(1).trim({ it <= ' ' })
      else
        txt
      str = str.substring(0, str.length - 1).trim({ it <= ' ' })
      if (str.equals("else", ignoreCase = true))
        return Expression.TRUE
      else
        return str
    }

  }

}
