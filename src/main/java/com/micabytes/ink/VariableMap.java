package com.micabytes.ink;

public interface VariableMap {
  void logException(Exception e);

  boolean hasVariable(String token);

  Object getValue(String token);

  boolean hasFunction(String token);

  Function getFunction(String token);

  boolean checkObject(String token);
}
