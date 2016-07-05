package com.micabytes.ink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Definition of a supported expression function. A function is defined by a name, the number of
 * parameters and the actual processing implementation.
 */
public class KnotFunction extends ParameterizedContainer implements Function {

  public KnotFunction(int l, String str) {
    lineNumber = l;
    type = ContentType.FUNCTION;
    level = 0;
    parent = null;
    String fullId = extractId(str);
    fullId = fullId.replaceFirst(Symbol.FUNCTION, "");
    if (fullId.contains(StoryText.BRACE_LEFT)) {
      String params = fullId.substring(fullId.indexOf(StoryText.BRACE_LEFT)+1, fullId.indexOf(StoryText.BRACE_RIGHT));
      String[] param = params.split(",");
      parameters = new ArrayList<>();
      for (String aParam : param) {
        if (!aParam.trim().isEmpty())
          parameters.add(aParam.trim());
      }
      fullId = fullId.substring(0, fullId.indexOf(StoryText.BRACE_LEFT)).trim();
    }
    id = fullId;
  }

  public static boolean isFunctionHeader(String str) {
    if (!str.startsWith(Symbol.FUNCTION_HEADER)) return false;
    String fullId = extractId(str);
    return fullId.startsWith(Symbol.FUNCTION);
  }

  private static String extractId(String str) {
    int pos = 0;
    while (InkParser.HEADER == str.charAt(pos)) {
      pos++;
    }
    StringBuilder header = new StringBuilder(pos + 1);
    for (int i=0; i<pos; i++)
      header.append(InkParser.HEADER);
    return str.replaceAll(header.toString(), "").trim();
  }

  @Override
  public int getNumParams() {
    return parameters.size();
  }

  @Override
  public boolean isVariableNumParams() {
    return false;
  }

  @SuppressWarnings("OverlyComplexMethod")
  @Override
  public Object eval(List<Object> params, VariableMap vmap) throws InkRunTimeException {
    Story story = (Story) vmap;
    if (params.size() != parameters.size())
      throw new InkRunTimeException("Parameters passed to function " + id + " do not match the definition of the function. Passed " + params.size() + " parameters and expected " + parameters.size());
    Container callingContainer = story.container;
    story.container = this;
    if (variables == null)
      variables = new HashMap<>();
    for (int i = 0; i< parameters.size(); i++) {
      variables.put(parameters.get(i), params.get(i));
    }
    for (Content c : content) {
      if (c.type == ContentType.TEXT) {
        story.container = callingContainer;
        return StoryText.getText(c.text, c.count, story);
      }
      else if (c.isVariable()) {
        Variable v = (Variable) c;
        if (v.isVariableReturn()) {
          variables.put(Symbol.RETURN, "");
          v.evaluate(story);
          story.container = callingContainer;
          return variables.get(Symbol.RETURN);
        }
        v.evaluate(story);
      }
      else if (c.isConditional()) {
        Conditional cond = (Conditional) c;
        cond.initialize(story, cond);
        for (int j=0; j<cond.getContentSize(); j++) {
          Content cd = cond.getContent(j);
          if (cd.type == ContentType.TEXT && !cd.text.isEmpty()) {
            story.container = callingContainer;
            return StoryText.getText(cd.text, cd.count, story);
          }
          if (cd.isVariable()) {
            Variable v = (Variable) cd;
            if (v.isVariableReturn()) {
              variables.put(Symbol.RETURN, "");
              v.evaluate(story);
              story.container = callingContainer;
              return variables.get(Symbol.RETURN);
            }
            v.evaluate(story);
          }
        }
      }
    }
    story.container = callingContainer;
    return "";
  }

}
