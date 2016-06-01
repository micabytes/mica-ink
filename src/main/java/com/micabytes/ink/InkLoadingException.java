package com.micabytes.ink;

@SuppressWarnings("unused")
public class InkLoadingException extends Exception {

  public InkLoadingException(String message) {
    super(message);
  }

  public InkLoadingException(Throwable throwable) {
    super(throwable);
  }

  public InkLoadingException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
