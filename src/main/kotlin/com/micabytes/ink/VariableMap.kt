package com.micabytes.ink

interface VariableMap {
  fun logException(e: Exception)
  fun hasVariable(token: String): Boolean
  fun getValue(token: String): Any
  fun hasFunction(token: String): Boolean
  fun getFunction(token: String): Function
  fun checkObject(token: String): Boolean
  fun debugInfo(): String
}
