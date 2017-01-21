package com.micabytes.ink


import java.math.BigDecimal
import java.util.Random

@SuppressWarnings("UtilityClass")
class StoryText @Throws(AssertionError::class)
private constructor() {

    init {
        throw AssertionError("StoryText should never be initialized")
    }

    companion object {
        internal val BRACE_RIGHT = ")"
        internal val BRACE_LEFT = "("
        internal val CBRACE_RIGHT = "}"
        internal val CBRACE_LEFT = "{"
        internal val SBRACE_LEFT = "["
        internal val SBRACE_RIGHT = "]"
         private val ERROR = "(ERROR:"
        private val COLON = ':'

        fun getText(text: String, count: Int, variables: VariableMap): String {
            var ret = text
            while (ret.contains(CBRACE_LEFT)) {
                val start = ret.lastIndexOf(CBRACE_LEFT)
                val end = ret.indexOf(CBRACE_RIGHT, start)
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
            val s = str.replace(CBRACE_LEFT, "").replace(CBRACE_RIGHT, "")
//            if (s.contains(":"))
//                return evaluateConditionalText(s, variables)
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
                return ERROR + s + BRACE_RIGHT
            }

        }

        private fun evaluateSequenceText(str: String, count: Int): String {
            val tokens = str.split("[|]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val i = if (count < tokens.size) count else tokens.size - 1
            return tokens[i]
        }

        private fun evaluateShuffleText(str: String): String {
            val s = str.substring(1)
            val tokens = s.split("[|]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val i = Random().nextInt(tokens.size)
            return tokens[i]
        }

        private fun evaluateOnceOnlyText(str: String, count: Int): String {
            val s = str.substring(1)
            val tokens = s.split("[|]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            return if (count < tokens.size) tokens[count] else ""
        }

        private fun evaluateCycleText(str: String, count: Int): String {
            val s = str.substring(1)
            val tokens = s.split("[|]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val i = count % tokens.size
            return tokens[i]
        }

        /*
        @SuppressWarnings("OverlyComplexMethod")
        private fun evaluateConditionalText(str: String, variables: VariableMap): String {
            if (str.startsWith("#")) {
                val condition = str.substring(1, str.indexOf(COLON.toInt())).trim({ it <= ' ' })
                val text = str.substring(str.indexOf(COLON.toInt()) + 1)
                val options = text.split("[|]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                var `val` = 0
                try {
                    val value = Declaration.evaluate(condition, variables)
                    if (value is Boolean) {
                        `val` = if (value) 1 else 0
                    } else if (value is BigDecimal) {
                        `val` = value.toInt()
                    } else {
                        `val` = if (value == null) 0 else 1
                    }
                } catch (e: InkRunTimeException) {
                    variables.logException(e)
                    // TODO: Change?
                }

                if (`val` >= options.size)
                    return options[options.size - 1]
                if (`val` < 0)
                    return options[0]
                return options[`val`]
            }
            // Regular conditional
            val condition = str.substring(0, str.indexOf(COLON.toInt())).trim({ it <= ' ' })
            val text = str.substring(str.indexOf(COLON.toInt()) + 1)
            val options = text.split("[|]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
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
    */
    }

}
