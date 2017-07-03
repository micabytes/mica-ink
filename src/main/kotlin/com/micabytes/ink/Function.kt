package com.micabytes.ink

interface Function {
  val numParams: Int
  val isFixedNumParams: Boolean
  fun eval(params: List<Any>, vMap: VariableMap): Any
}
