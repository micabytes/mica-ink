package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

class Stitch extends ParameterizedContainer {

  public Stitch(int l, String str, @Nullable Container current) throws InkParseException {
    lineNumber = l;
    level = 1;
    if (current == null)
      throw new InkParseException("A stitch cannot be defined without a parent Knot");
    parent = current.getContainer(0);
    parent.add(this);
    type = ContentType.STITCH;
    String fullId = new StringBuilder(parent.id)
        .append(InkParser.DOT)
        .append(str.replaceAll(String.valueOf(InkParser.HEADER), "").trim())
        .toString();
    if (fullId.contains(BRACE_LEFT)) {
      String params = fullId.substring(fullId.indexOf(BRACE_LEFT)+1, fullId.indexOf(BRACE_RIGHT));
      String[] param = params.split(",");
      parameters = new ArrayList<>();
      for (int i=0; i<param.length; i++)
        parameters.add(param[i]);
      fullId = fullId.substring(0, fullId.indexOf(BRACE_LEFT));
    }
    //if (fullId.startsWith(FUNCTION)) {
    //
    //}
    id = fullId;
  }

  public static boolean isStitchHeader(String str) {
    if (str.length() < 2) return false;
    if (str.charAt(0) == InkParser.HEADER && str.charAt(1) != InkParser.HEADER)
      return true;
    return false;
  }

}
