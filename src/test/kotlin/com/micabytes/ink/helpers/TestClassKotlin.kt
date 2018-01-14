package com.micabytes.ink.helpers

import java.math.BigDecimal

@Suppress("unused")
class TestClassKotlin {

  fun hello(): String {
    return "Hello, is it me you're looking for?"
  }

  fun number(b: BigDecimal): String {
    return "Mambo Number " + b.toPlainString()
  }

  fun wrong(): Boolean {
    return false
  }

}
