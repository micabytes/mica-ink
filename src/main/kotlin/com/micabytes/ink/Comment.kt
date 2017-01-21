package com.micabytes.ink

import java.math.BigDecimal
import java.util.ArrayList

internal class Comment(lineNumber: Int,
                       content: String,
                       parent: Container?) : Content(lineNumber, content, parent) {
    private var conditions: ArrayList<String>? = null

    /*
    init {
        lineNumber = l
        val notation = str[0]
        type = if (notation == InkParser.CHOICE_DOT) ContentType.COMMENT_ONCE else ContentType.COMMENT_REPEATABLE
        val s = str.substring(1).trim({ it <= ' ' })
        addLine(s)
    }

    private fun addLine(str: String) {
        var s = str.trim { it <= ' ' }
        if (s.startsWith(StoryText.CBRACE_LEFT) && conditions == null)
            conditions = ArrayList<String>()
        while (s.startsWith("{")) {
            val c = s.substring(s.indexOf(StoryText.CBRACE_LEFT) + 1, s.indexOf(StoryText.CBRACE_RIGHT)).trim({ it <= ' ' })
            conditions!!.add(c)
            s = s.substring(s.indexOf(StoryText.CBRACE_RIGHT) + 1).trim({ it <= ' ' })
        }
        text = s
    }

    @Throws(InkRunTimeException::class)
    fun evaluateConditions(story: Story): Boolean {
        if (conditions == null)
            return true
        for (condition in conditions!!) {
            val obj = Declaration.evaluate(condition, story) ?: return false
            if (obj is Boolean && !obj)
                return false
            if (obj is BigDecimal && obj.toInt() <= 0)
                return false
        }
        return true
    }

    @Throws(InkRunTimeException::class)
    fun getCommentText(story: Story): String {
        return StoryText.getText(text, count, story)
    }
    */
}