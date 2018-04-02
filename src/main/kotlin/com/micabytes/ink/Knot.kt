package com.micabytes.ink

import com.micabytes.ink.util.InkRunTimeException

internal class Knot(header: String,
                    lineNumber: Int) :
    ParameterizedContainer(Knot.getId(header), ParameterizedContainer.getParameters(header), null, lineNumber),
    Function {

  internal val isFunction: Boolean = Knot.isFunction(header)

  override val isFixedNumParams = true

  override val numParams = parameters.size

  override fun eval(params: List<Any>, vMap: VariableMap): Any {
    // Should probably do some checks when creating functions to ensure no illegal items are added.
    // Note - this resolution will have some issue if local variables are impleemented.
    val story = vMap as Story
    if (params.size != parameters.size)
      throw InkRunTimeException("Parameters passed to function " + id + " do not match the definition of the function. Passed " + params.size + " parameters and expected " + parameters.size)
    val callingContainer = story.container
    story.container = this
    for (i in parameters.indices)
      values[parameters[i]] = params[i]
    for (c in children) {
      when (c) {
        is Declaration -> {
          if (c.text.startsWith(Symbol.RETURN)) {
            values[Symbol.RETURN] = ""
            c.evaluate(story)
            story.container = callingContainer
            return values[Symbol.RETURN]!!
          }
          else
            c.evaluate(story)
        }
        is Conditional -> {
          val opt = c.resolveConditional(story)
          for (oc in opt.children) {
            when (oc) {
              is Declaration -> {
                if (oc.text.startsWith(Symbol.RETURN)) {
                  values[Symbol.RETURN] = ""
                  oc.evaluate(story)
                  story.container = callingContainer
                  return values[Symbol.RETURN]!!
                } else
                  oc.evaluate(story)
              }
              else -> {
                story.container = callingContainer
                return oc.getText(story)
              }
            }
          }
        }
        else -> {
          story.container = callingContainer
          return c.getText(story)
        }
      }
    }
    story.container = callingContainer
    return 0
  }

  companion object {

    private const val KNOT_HEADER = "=="

    fun isKnot(str: String): Boolean {
      return str.startsWith(KNOT_HEADER)
    }

    fun getId(id: String): String {
      var pos = 0
      while (Symbol.HEADER == id[pos]) {
        pos++
      }
      val header = StringBuilder(pos + 1)
      for (i in 0 until pos)
        header.append(Symbol.HEADER)
      var fullId = id.replace(header.toString().toRegex(), "").trim({ it <= ' ' })
      if (fullId.startsWith(Symbol.FUNCTION)) {
        fullId = fullId.replaceFirst(Symbol.FUNCTION.toRegex(), "")
      }
      if (fullId.contains(Symbol.BRACE_LEFT)) {
        fullId = fullId.substring(0, fullId.indexOf(Symbol.BRACE_LEFT))
      }
      return fullId.trim({ it <= ' ' })
    }

    fun isFunction(id: String): Boolean {
      val strippedId = id.replace(Symbol.HEADER.toString().toRegex(), "").trim({ it <= ' ' })
      return (strippedId.startsWith(Symbol.FUNCTION))
    }

  }

}
