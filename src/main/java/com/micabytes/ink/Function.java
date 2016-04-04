package com.micabytes.ink;

import java.util.List;

public interface Function {

  public String getId();

  public int getNumParams();

  public boolean numParamsVaries();

  /**
   * Evaluation of the function
   *
   * @param parameters Parameters will be passed by the expression evaluator as a {@link List} of {@link Object} values.
   * @return The function must return a new {@link Object} value as a computing result.
   */
  public Object eval(List<Object> parameters, Story story) throws InkRunTimeException;

}
