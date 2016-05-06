package com.micabytes.ink;

import java.math.BigDecimal;
import java.util.Random;

public class Content {

  String id;
  int lineNumber;
  ContentType type = ContentType.TEXT;
  String text = "";
  int count;

  public Content() {
    // NOOP
  }

  public Content(int l, String str, Container current) {
    lineNumber = l;
    text = str;
    current.add(this);
  }

  public String getId() {
    return id;
  }

  void setId(String s) {
    id = s;
  }

  public String generateId(Container p) {
    int i = p.getContentIndex(this);
    id = p.getId() + InkParser.DOT + Integer.toString(i);
    return id;
  }

  public boolean isKnot() {
    return type == ContentType.KNOT;
  }

  public boolean isFunction() {
    return type == ContentType.FUNCTION;
  }

  public boolean isStitch() {
    return type == ContentType.STITCH;
  }

  public boolean isChoice() {
    return type == ContentType.CHOICE_ONCE || type == ContentType.CHOICE_REPEATABLE;
  }

  public boolean isFallbackChoice() {
    return isChoice() && text.isEmpty();
  }

  public boolean isGather() {
    return type == ContentType.GATHER;
  }

  public boolean isDivert() {
    return text.contains(Symbol.DIVERT) && !isVariable();
  }

  public boolean isConditional() {
    return type == ContentType.CONDITIONAL || type == ContentType.SEQUENCE_CYCLE || type == ContentType.SEQUENCE_ONCE || type == ContentType.SEQUENCE_SHUFFLE || type == ContentType.SEQUENCE_STOP;
  }

  public int getCount() {
    return count;
  }

  public void increment() {
    count ++;
  }

  public boolean isVariable() {
    return type == ContentType.VARIABLE_DECLARATION || type == ContentType.VARIABLE_EXPRESSION || type == ContentType.VARIABLE_RETURN;
  }

  public boolean isVariableReturn() {
    return type == ContentType.VARIABLE_RETURN;
  }

  public boolean isContainer() {
    return isKnot() || isFunction() || isStitch() || isChoice() || isGather();
  }

}
