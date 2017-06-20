package com.micabytes.ink

import java.util.*

internal open class ParameterizedContainer(id: String,
                                           val parameters: List<String>,
                                           parent: Container?,
                                           lineNumber: Int) : Container(id, "", parent, lineNumber) {
  internal val values: HashMap<String, Any> = HashMap()

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
      val params: ArrayList<String> = ArrayList()
      if (header.contains(Symbol.BRACE_LEFT)) {
        val paramStr = header.substring(header.indexOf(Symbol.BRACE_LEFT) + 1, header.indexOf(Symbol.BRACE_RIGHT))
        val param = paramStr.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        param.mapTo(params) { aParam -> aParam.trim({ it <= ' ' }) }
      }
      return params
    }

  }

}
