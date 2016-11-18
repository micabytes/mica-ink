package com.micabytes.ink

import java.util.ArrayList

internal class Stitch
@SuppressWarnings("StringBufferReplaceableByString")
@Throws(InkParseException::class)
constructor(l: Int, str: String, current: Container?) : ParameterizedContainer() {
    init {
        lineNumber = l
        level = 1
        if (current == null)
            throw InkParseException("A stitch cannot be defined without a parent Knot")
        parent = current.getContainer(0)
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
