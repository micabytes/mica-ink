package com.micabytes.ink

// TODO: Functions should check that stitches are not added.

internal class KnotFunction(lineNumber: Int,
                    text: String) : ParameterizedContainer(text, ParameterizedContainer.getParameters(text), null, lineNumber) {

  /*
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
      //val params = fullId.substring(fullId.indexOf(StoryText.BRACE_LEFT) + 1, fullId.indexOf(StoryText.BRACE_RIGHT))
      //val param = params.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
      //param.mapTo(parameters) { aParam -> aParam.trim({ it <= ' ' }) }
      fullId = fullId.substring(0, fullId.indexOf(StoryText.BRACE_LEFT))
    }
    id = fullId
  }

  companion object {
    private val KNOT_HEADER = "=="
    fun isFunctionHeader(str: String): Boolean {
      return str.startsWith(KNOT_HEADER)
    }
  }
*/
}

/**
 * Definition of a supported expression function. A function is defined by a name, the number of
 * parameters and the actual processing implementation.
 */
/*
class KnotFunction(l: Int, str: String) : ParameterizedContainer(), Function {

    init {
        lineNumber = l
        type = ContentType.FUNCTION
        level = 0
        parent = null
        var fullId = extractId(str)
        fullId = fullId.replaceFirst(Symbol.FUNCTION.toRegex(), "")
        if (fullId.contains(StoryText.BRACE_LEFT)) {
            val params = fullId.substring(fullId.indexOf(StoryText.BRACE_LEFT) + 1, fullId.indexOf(StoryText.BRACE_RIGHT))
            val param = params.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            parameters = ArrayList<String>()
            for (aParam in param) {
                if (!aParam.trim({ it <= ' ' }).isEmpty())
                    parameters!!.add(aParam.trim({ it <= ' ' }))
            }
            fullId = fullId.substring(0, fullId.indexOf(StoryText.BRACE_LEFT)).trim({ it <= ' ' })
        }
        id = fullId
    }

    override val numParams: Int
        get() = parameters!!.size

    override val isFixedNumParams: Boolean
        get() = false

    @SuppressWarnings("OverlyComplexMethod")
    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any {
        val story = vmap as Story
        if (params.size != parameters!!.size)
            throw InkRunTimeException("Parameters passed to function " + id + " do not match the definition of the function. Passed " + params.size + " parameters and expected " + parameters!!.size)
        val callingContainer = story.container
        story.container = this
        if (values == null)
            values = HashMap<String, Any>()
        for (i in parameters!!.indices) {
            values!!.put(parameters!![i], params[i])
        }
        for (c in children) {
            if (c.type == ContentType.TEXT) {
                story.container = callingContainer
                return StoryText.getText(c.text, c.count, story)
            } else if (c.isVariable) {
                val v = c as Declaration
                if (v.isVariableReturn) {
                    values!!.put(Symbol.RETURN, "")
                    v.evaluate(story)
                    story.container = callingContainer
                    return values!![Symbol.RETURN]
                }
                v.evaluate(story)
            } else if (c.isConditional) {
                val cond = c as Conditional
                cond.initialize(story, cond)
                for (j in 0..cond.size - 1) {
                    val cd = cond.get(j)
                    if (cd.type == ContentType.TEXT && !cd.text.isEmpty()) {
                        story.container = callingContainer
                        return StoryText.getText(cd.text, cd.count, story)
                    }
                    if (cd.isVariable) {
                        val v = cd as Declaration
                        if (v.isVariableReturn) {
                            values!!.put(Symbol.RETURN, "")
                            v.evaluate(story)
                            story.container = callingContainer
                            return values!![Symbol.RETURN]
                        }
                        v.evaluate(story)
                    }
                }
            }
        }
        story.container = callingContainer
        return ""
    }

    companion object {

        fun isFunctionHeader(str: String): Boolean {
            if (!str.startsWith(Symbol.FUNCTION_HEADER)) return false
            val fullId = extractId(str)
            return fullId.startsWith(Symbol.FUNCTION)
        }

        private fun extractId(str: String): String {
            var pos = 0
            while (InkParser.HEADER == str[pos]) {
                pos++
            }
            val header = StringBuilder(pos + 1)
            for (i in 0..pos - 1)
                header.append(InkParser.HEADER)
            return str.replace(header.toString().toRegex(), "").trim({ it <= ' ' })
        }
    }

}
*/
