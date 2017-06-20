package com.micabytes.ink

import com.micabytes.ink.exception.InkRunTimeException
import java.math.BigDecimal
import java.util.Random

@SuppressWarnings("UtilityClass")
class StoryText @Throws(AssertionError::class)
private constructor() {

  init {
    throw AssertionError("StoryText should never be initialized")
  }

  companion object {
    private val ERROR = "(ERROR:"

    fun getText(text: String, count: Int, variables: VariableMap): String {
      var ret = text
      while (ret.contains(Symbol.CBRACE_LEFT)) {
        val start = ret.lastIndexOf(Symbol.CBRACE_LEFT)
        val end = ret.indexOf(Symbol.CBRACE_RIGHT, start)
        if (end < 0) {
          variables.logException(InkRunTimeException("Mismatched curly braces in text: " + text))
          return ret
        }
        val s = ret.substring(start, end + 1)
        val res = evaluateText(s, count, variables)
        ret = ret.replace(s, res)
      }
      return ret
    }

    private fun evaluateText(str: String, count: Int, variables: VariableMap): String {
      val s = str.replace(Symbol.CBRACE_LEFT.toString(), "").replace(Symbol.CBRACE_RIGHT.toString(), "")
      if (s.contains(":"))
        return evaluateConditionalText(s, variables)
      if (s.startsWith("&"))
        return evaluateCycleText(s, count)
      if (s.startsWith("!"))
        return evaluateOnceOnlyText(s, count)
      if (s.startsWith("~"))
        return evaluateShuffleText(s)
      if (s.contains("|"))
        return evaluateSequenceText(s, count)
      return evaluateTextVariable(s, variables)
    }

    private fun evaluateTextVariable(s: String, variables: VariableMap): String {
      try {
        val obj = Declaration.evaluate(s, variables)
        if (obj is BigDecimal)
        // We don't want BigDecimal canonical form
          return obj.toPlainString()
        return obj.toString()
      } catch (e: InkRunTimeException) {
        variables.logException(e)
        return ERROR + s + Symbol.BRACE_RIGHT
      }

    }

    private fun evaluateSequenceText(str: String, count: Int): String {
      val tokens = str.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      val i = if (count < tokens.size) count else tokens.size - 1
      return tokens[i]
    }

    private fun evaluateShuffleText(str: String): String {
      val s = str.substring(1)
      val tokens = s.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      val i = Random().nextInt(tokens.size)
      return tokens[i]
    }

    private fun evaluateOnceOnlyText(str: String, count: Int): String {
      val s = str.substring(1)
      val tokens = s.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      return if (count < tokens.size) tokens[count] else ""
    }

    private fun evaluateCycleText(str: String, count: Int): String {
      val s = str.substring(1)
      val tokens = s.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      val i = count % tokens.size
      return tokens[i]
    }

    private fun evaluateConditionalText(str: String, variables: VariableMap): String {
      //if (str.startsWith("when")) {
      //  return evaluateWhen(str, variables)
      //}
      if (str.startsWith("?")) {
        val condition = str.substring(1, str.indexOf(Symbol.COLON)).trim({ it <= ' ' })
        val text = str.substring(str.indexOf(Symbol.COLON) + 1)
        val options = text.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        var v = 0
        try {
          val value = Declaration.evaluate(condition, variables)
          if (value is Boolean) {
            v = if (value) 1 else 0
          } else if (value is BigDecimal) {
            v = value.toInt()
          } else {
            v = 1
          }
        } catch (e: InkRunTimeException) {
          variables.logException(e)
          // TODO: Change?
        }

        if (v >= options.size)
          return options[options.size - 1]
        if (v < 0)
          return options[0]
        return options[v]
      }
      // Regular conditional
      val condition = str.substring(0, str.indexOf(Symbol.COLON)).trim({ it <= ' ' })
      val text = str.substring(str.indexOf(Symbol.COLON) + 1)
      val options = text.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      if (options.size > 2)
        variables.logException(InkRunTimeException("Too many options in a conditional text."))
      val ifText = options[0]
      val elseText = if (options.size == 1) "" else options[1]
      try {
        val obj = Declaration.evaluate(condition, variables)
        if (obj is BigDecimal)
          return if ((obj as Number).toInt() > 0) ifText else elseText
        if (obj is Boolean)
          return if (obj) ifText else elseText
        variables.logException(InkRunTimeException("Condition in conditional text did not resolve into a number or boolean."))
        return elseText
      } catch (e: InkRunTimeException) {
        variables.logException(e)
        return elseText
      }

    }

    private fun evaluateWhen(str: String, variables: VariableMap): String {
      /*
      val condition = str.substring(0, str.indexOf(Symbol.COLON)).trim({ it <= ' ' }).replaceFirst("when", "")
      val text = str.substring(str.indexOf(Symbol.COLON) + 1)
      val options = text.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      var v = 0
      var vStr: String? = null
      try {
        val value = Declaration.evaluate(condition, variables)
        when (value) {
          is Boolean -> v = if (value) 1 else 0
          is BigDecimal -> v = value.toInt()
          is String -> vStr = value
          else -> v = 1
        }
      } catch (e: InkRunTimeException) {
        variables.logException(e)
      }
      for (option in options) {
        val opt = option.split("=>").toTypedArray()
        if (opt[0][0].isDigit) {
          val d = opt[0].toDouble()
          if ()


        }
        return opt[1]
      }
      */
      return ""
    }

  }

}
