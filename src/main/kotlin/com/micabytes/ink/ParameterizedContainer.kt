package com.micabytes.ink

import java.util.*

internal open class ParameterizedContainer(id: String,
                                           val parameters: List<String>,
                                           parent: Container?,
                                           lineNumber: Int) : Container(id, "", parent, lineNumber) {
  internal val values: HashMap<String, Any> = HashMap<String, Any>()

  fun hasValue(key: String): Boolean {
    return values.containsKey(key)
  }

  fun getValue(key: String): Any {
    return values[key]!!
  }

  fun setValue(key: String, value: Any) {
    values.put(key, value)
  }

  companion object {

    fun getParameters(header: String): List<String> {
      val params: ArrayList<String> = ArrayList<String>()
      if (header.contains(StoryText.BRACE_LEFT)) {
        val paramStr = header.substring(header.indexOf(StoryText.BRACE_LEFT) + 1, header.indexOf(StoryText.BRACE_RIGHT))
        val param = paramStr.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        param.mapTo(params) { aParam -> aParam.trim({ it <= ' ' }) }
      }
      return params
    }

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
            if (values == null)
                values = vs
            else {
                values!!.clear()
                values!!.putAll(vs)
            }
        }
    }


    fun getParameters(): List<String> {
        return Collections.unmodifiableList(parameters!!)
    }

    fun setParameters(params: ArrayList<String>) {
        parameters = ArrayList(params)
    }

    */

}
