package com.micabytes.ink

import java.math.BigDecimal

/**
 * Abstract definition of a supported operator. An operator is defined by its name (pattern), precedence and if it is
 * left- or right associative.
 */
abstract class Operator(val oper: String, val precedence: Int, val isLeftAssoc: Boolean) {
  abstract fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal
}

