package com.micabytes.ink;

import java.util.ArrayList;

// TODO: Functions should check that stitches are not added.

class Knot extends ParameterizedContainer {
  private static final String KNOT_HEADER = "==";
  private static final String FUNCTION = "function";

  public Knot(int l, String str) {
    lineNumber = l;
    int pos = 0;
    while (InkParser.HEADER == str.charAt(pos)) {
      pos++;
    }
    StringBuilder header = new StringBuilder(pos + 1);
    for (int i=0; i<pos; i++)
      header.append(InkParser.HEADER);
    type = ContentType.KNOT;
    level = 0;
    parent = null;
    String fullId = str.replaceAll(header.toString(), "").trim();
    if (fullId.startsWith("function")) {
      type = ContentType.FUNCTION;
      fullId = fullId.replaceFirst("function", "");
    }
    if (fullId.contains(StoryText.BRACE_LEFT)) {
      String params = fullId.substring(fullId.indexOf(StoryText.BRACE_LEFT)+1, fullId.indexOf(StoryText.BRACE_RIGHT));
      String[] param = params.split(",");
      parameters = new ArrayList<>();
      for (int i=0; i<param.length; i++)
        parameters.add(param[i].trim());
      fullId = fullId.substring(0, fullId.indexOf(StoryText.BRACE_LEFT));
    }
    //if (fullId.startsWith(FUNCTION)) {
    //
    //}
    id = fullId;
  }

  public static boolean isKnotHeader(String str) {
    return str.startsWith(KNOT_HEADER);
  }

}
