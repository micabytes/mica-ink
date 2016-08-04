package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ParameterizedContainer extends Container {
  List<String> parameters;
  Map<String, Object> variables;

  @Override
  public void initialize(Story story, Content c) throws InkRunTimeException {
    super.initialize(story, c);
    String d = c.text.substring(c.text.indexOf(Symbol.DIVERT) + 2).trim();
    if (d.contains(StoryText.BRACE_LEFT) && parameters != null) {
      String params = d.substring(d.indexOf(StoryText.BRACE_LEFT) + 1, d.indexOf(StoryText.BRACE_RIGHT));
      String[] param = params.split(",");
      if (param.length != parameters.size())
        throw new InkRunTimeException("LineNumber: " + c.lineNumber + ". Mismatch in the parameter declaration in the call to " + id);
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

  List<String> getParameters() {
    return Collections.unmodifiableList(parameters);
  }

  void setParameters(ArrayList<String> params) {
    parameters = new ArrayList<>(params);
  }

  @Nullable
  Map<String, Object> getVariables() {
    if (variables == null) return null;
    return Collections.unmodifiableMap(variables);
  }

  void setVariables(HashMap<String, Object> vars) {
    variables = new HashMap<>(vars);
  }
}
