package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.util.List;

public interface Function {
  int getNumParams();
  boolean isFixedNumParams();
  /**
   * Evaluation of the function
   *
   * @param params Parameters will be passed by the expression evaluator as a {@link List} of {@link Object} values.
   * @param vmap
   * @return The function must return a new {@link Object} value as a computing result.
   */
  Object eval(List<Object> params, VariableMap vmap) throws InkRunTimeException;

}
