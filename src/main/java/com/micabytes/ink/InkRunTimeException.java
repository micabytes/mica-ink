package com.micabytes.ink;

@SuppressWarnings("unused")
public class InkRunTimeException extends Exception {

  public InkRunTimeException(String message) {
    super(message);
  }

  public InkRunTimeException(Throwable throwable) {
    super(throwable);
  }

  public InkRunTimeException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
