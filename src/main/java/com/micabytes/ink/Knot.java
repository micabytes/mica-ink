package com.micabytes.ink;

class Knot extends Container {
  private static final String KNOT_HEADER = "==";

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
    id = str.replaceAll(header.toString(), "").trim();
    level = 0;
    parent = null;
  }

  public static boolean isKnotHeader(String str) {
    return str.startsWith(KNOT_HEADER);
  }
  
}
