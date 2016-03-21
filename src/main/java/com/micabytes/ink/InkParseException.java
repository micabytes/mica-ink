package com.micabytes.ink;

@SuppressWarnings("unused")
public class InkParseException extends Exception {

  public InkParseException(String message) {
    super(message);
  }

  public InkParseException(Throwable throwable) {
    super(throwable);
  }

  public InkParseException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
