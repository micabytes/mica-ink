package com.micabytes.ink;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ParameterizedContainer extends Container {
  protected ArrayList<String> parameters;
  protected HashMap<String, Object> variables;

  @Override
  public void initialize(Story story, Content content) throws InkRunTimeException {
    super.initialize(story, content);
    String d = content.text.substring(content.text.indexOf(Story.DIVERT) + 2).trim();
    if (d.contains(Content.BRACE_LEFT) && parameters != null) {
      String params = d.substring(d.indexOf(BRACE_LEFT), d.indexOf(BRACE_RIGHT));
      String[] param = params.split(",");
      if (param.length != parameters.size())
        throw new InkRunTimeException("LineNumber: " + content.lineNumber + ". Mismatch in the parameter declaration in the call to " + id);
      variables = new HashMap<>();
      for (int i=0; i<param.length; i++) {
        String p = param[i];
        variables.put(parameters.get(i), story.getValue(p));
      }
    }
  }

  boolean hasValue(String key) {
    if (variables == null) return false;
    return variables.containsKey(key);
  }

  Object getValue(String key) {
    return variables.get(key);
  }
}
