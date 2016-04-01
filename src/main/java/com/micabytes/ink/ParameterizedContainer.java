package com.micabytes.ink;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class ParameterizedContainer extends Container {
  protected ArrayList<String> parameters;
  protected HashMap<String, Object> variables;

  @Override
  public void initialize(Story story, Content content) throws InkRunTimeException {
    super.initialize(story, content);
    String d = content.text.substring(content.text.indexOf(Story.DIVERT) + 2).trim();
    if (d.contains(Content.BRACE_LEFT) && parameters != null) {
      String params = d.substring(d.indexOf(BRACE_LEFT) + 1, d.indexOf(BRACE_RIGHT));
      String[] param = params.split(",");
      if (param.length != parameters.size())
        throw new InkRunTimeException("LineNumber: " + content.lineNumber + ". Mismatch in the parameter declaration in the call to " + id);
      HashMap<String, Object> vs = new HashMap<>();
      for (int i=0; i<param.length; i++) {
        String p = param[i].trim();
        vs.put(parameters.get(i),Variable.evaluate(p, story));
      }
      if (variables == null)
        variables = vs;
      else {
        variables.clear();
        variables.putAll(vs);
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

  void setValue(String key, Object value) {
    if (variables == null) variables = new HashMap<>();
    variables.put(key, value);
  }

}
