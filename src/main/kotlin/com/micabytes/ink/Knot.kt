package com.micabytes.ink

import java.util.ArrayList

// TODO: Functions should check that stitches are not added.

internal class Knot(l: Int, str: String) : ParameterizedContainer() {

    init {
        lineNumber = l
        var pos = 0
        while (InkParser.HEADER == str[pos]) {
            pos++
        }
        val header = StringBuilder(pos + 1)
        for (i in 0..pos - 1)
            header.append(InkParser.HEADER)
        type = ContentType.KNOT
        level = 0
        parent = null
        var fullId = str.replace(header.toString().toRegex(), "").trim({ it <= ' ' })
        if (fullId.startsWith(Symbol.FUNCTION)) {
            type = ContentType.FUNCTION
            fullId = fullId.replaceFirst(Symbol.FUNCTION.toRegex(), "")
        }
        if (fullId.contains(StoryText.BRACE_LEFT)) {
            val params = fullId.substring(fullId.indexOf(StoryText.BRACE_LEFT) + 1, fullId.indexOf(StoryText.BRACE_RIGHT))
            val param = params.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            parameters = ArrayList<String>()
            for (aParam in param) parameters!!.add(aParam.trim({ it <= ' ' }))
            fullId = fullId.substring(0, fullId.indexOf(StoryText.BRACE_LEFT))
        }
        //if (fullId.startsWith(FUNCTION)) {
        //
        //}
        id = fullId
    }

    companion object {
        private val KNOT_HEADER = "=="

        fun isKnotHeader(str: String): Boolean {
            return str.startsWith(KNOT_HEADER)
        }
    }

}
