package com.micabytes.ink

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

internal open class ParameterizedContainer(lineNumber: Int,
                                           content: String,
                                           parent: Container?) : Container(lineNumber, content, parent) {

  internal val parameters: ArrayList<String> = ArrayList<String>()
  internal val variables: HashMap<String, Any> = HashMap<String, Any>()

  fun hasValue(key: String): Boolean {
    return variables.containsKey(key)
  }

  fun getValue(key: String): Any {
    return variables[key]!!
  }

  fun setValue(key: String, value: Any) {
    variables.put(key, value)
  }

    /*

    @Throws(InkRunTimeException::class)
    override fun initialize(story: Story, c: Content) {
        super.initialize(story, c)
        val d = c.text.substring(c.text.indexOf(Symbol.DIVERT) + 2).trim({ it <= ' ' })
        if (d.contains(StoryText.BRACE_LEFT) && parameters != null) {
            val params = d.substring(d.indexOf(StoryText.BRACE_LEFT) + 1, d.indexOf(StoryText.BRACE_RIGHT))
            val param = params.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (param.size != parameters!!.size)
                throw InkRunTimeException("LineNumber: " + c.lineNumber + ". Mismatch in the parameter declaration in the call to " + id)
            val vs = HashMap<String, Any>()
            for (i in param.indices) {
                val p = param[i].trim({ it <= ' ' })
                vs.put(parameters!![i], Declaration.evaluate(p, story))
            }
            if (variables == null)
                variables = vs
            else {
                variables!!.clear()
                variables!!.putAll(vs)
            }
        }
    }


    fun getParameters(): List<String> {
        return Collections.unmodifiableList(parameters!!)
    }

    fun setParameters(params: ArrayList<String>) {
        parameters = ArrayList(params)
    }

    fun getVariables(): Map<String, Any>? {
        if (variables == null) return null
        return Collections.unmodifiableMap(variables!!)
    }

    fun setVariables(vars: HashMap<String, Any>) {
        variables = HashMap(vars)
    }
    */

}
