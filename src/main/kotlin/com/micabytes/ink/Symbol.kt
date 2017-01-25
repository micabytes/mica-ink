package com.micabytes.ink


/** Utility class for Ink constants/symbols
 */
@SuppressWarnings("UtilityClass")
class Symbol private constructor() {

  init {
    throw AssertionError("Symbol should never be initialized.")
  }

  companion object {
    internal val BRACE_LEFT = '('
    internal val BRACE_RIGHT = ')'
    internal val DASH = '-'
    internal val FUNCTION_HEADER = "=="
    internal val GLUE = "<>"
    val DIVERT = "->"
    internal val DIVERT_END = "END"
    internal val THIS = "this"
    val EVENT = "event"
    internal val FUNCTION = "function"
    internal val RETURN = "return"
    internal val RETURNEQ = "return ="
  }
}
