package com.micabytes.ink;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class TestClass {

  public String hello() {
    return "Hello, is it me you're looking for?";
  }

  public String number(BigDecimal b) {
    return "Mambo Number " + b.toPlainString();
  }

  public boolean wrong() {
    return false;
  }

}
