package com.micabytes.ink;

import java.util.List;

public interface InkGameObject {

  public Object getAttribute(String id);

  public Object evaluateFunction(String funcName, List<Object> params, Story story);

}
