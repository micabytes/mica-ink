package com.micabytes.ink

internal class Gather @Throws(InkParseException::class)
constructor(lineNumber: Int,
            content: String,
            parent: Container?) : Content(lineNumber, content, parent) {
/*
    init {
        lineNumber = l
        type = ContentType.GATHER
        level = 2
        var s = str.substring(1).trim({ it <= ' ' })
        while (s.startsWith("- ")) {
            level++
            s = s.substring(1).trim({ it <= ' ' })
        }
        if (current == null)
            throw InkParseException("A gather must be nested within another knot, parent or choice/gather structure")
        parent = current.getParent(level - 1)
        parent!!.add(this)
        addLine(s)
    }

    private fun addLine(str: String) {
        var s = str
        if (s.startsWith("(")) {
            id = s.substring(s.indexOf(Symbol.BRACE_LEFT.toInt()) + 1, s.indexOf(Symbol.BRACE_RIGHT.toInt())).trim({ it <= ' ' })
            id = (if (parent != null) parent!!.id else null) + InkParser.DOT + id
            s = s.substring(s.indexOf(Symbol.BRACE_RIGHT.toInt()) + 1).trim({ it <= ' ' })

        }
        //noinspection ResultOfObjectAllocationIgnored
        Content(lineNumber, s, this)
    }

    companion object {

        fun isGatherHeader(str: String): Boolean {
            if (str.length < 2) return false
            return str.startsWith("- ")
        }
    }
*/
}
