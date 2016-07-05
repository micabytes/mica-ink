package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

/** Utility class for Ink constants/symbols
 */
@SuppressWarnings("UtilityClass")
public final class Symbol {
  static final char BRACE_LEFT = '(';
  static final char BRACE_RIGHT = ')';
  static final String FUNCTION_HEADER = "==";
  @NonNls static final String GLUE = "<>";
  @NonNls public static final String DIVERT = "->";
  @NonNls static final String DIVERT_END = "END";
  @NonNls static final String THIS = "this";
  @NonNls static final String EVENT = "event";
  @NonNls static final String FUNCTION = "function";
  @NonNls static final String RETURN = "return";

  private Symbol() {
    throw new AssertionError("Symbol should never be initialized.");
  }
}
