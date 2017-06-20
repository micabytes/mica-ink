package com.micabytes.ink

import com.micabytes.ink.exception.InkRunTimeException

interface Function {
  val numParams: Int
  val isFixedNumParams: Boolean
  @Throws(InkRunTimeException::class)
  fun eval(params: List<Any>, vMap: VariableMap): Any
}
