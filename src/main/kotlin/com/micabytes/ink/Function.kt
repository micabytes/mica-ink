package com.micabytes.ink

import java.util.*

interface LazyNumber {
  fun eval(): Any
}

interface Function {
  fun numParams(): Int
  fun isFixedNumParams(): Boolean
  fun eval(params: List<Any>, vMap: VariableMap): Any
}

/*
abstract class LazyFunction(val name: String, val numParams: Int) {
  val isFixedNumParams: Boolean = (numParams > 0)
  abstract fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber
}

/**
 * Abstract definition of a supported expression function. A function is defined by a name, the number of parameters
 * and the actual processing implementation.
 */
abstract class Function(name: String, numParams: Int) : LazyFunction(name.toUpperCase(Locale.US), numParams) {

  override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
    val params = ArrayList<Any>()
    for (lazyParam in lazyParams) {
      params.add(lazyParam.eval())
    }
    return object : LazyNumber {
      override fun eval(): Any {        return this@Function.eval(params)
      }
    }
  }

  abstract fun eval(parameters: List<Any>): Any
}
*/



/*
    addFunction(object : Function("SIN", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.sin(Math.toRadians(parameters[0]
            .toDouble()))
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("COS", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.cos(Math.toRadians(parameters[0]
            .toDouble()))
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("TAN", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.tan(Math.toRadians(parameters[0]
            .toDouble()))
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("ASIN", 1) { // added by av
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.toDegrees(Math.asin(parameters[0]
            .toDouble()))
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("ACOS", 1) { // added by av
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.toDegrees(Math.acos(parameters[0]
            .toDouble()))
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("ATAN", 1) { // added by av
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.toDegrees(Math.atan(parameters[0]
            .toDouble()))
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("SINH", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.sinh(parameters[0].toDouble())
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("COSH", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.cosh(parameters[0].toDouble())
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("TANH", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.tanh(parameters[0].toDouble())
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("RAD", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.toRadians(parameters[0].toDouble())
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("DEG", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.toDegrees(parameters[0].toDouble())
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("LOG", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.log(parameters[0].toDouble())
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("LOG10", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val d = Math.log10(parameters[0].toDouble())
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("SQRT", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val x = parameters[0]
        if (x.compareTo(BigDecimal.ZERO) == 0) {
          return BigDecimal(0)
        }
        if (x.signum() < 0) {
          throw ExpressionException(
              "Argument to SQRT() function must not be negative")
        }
        val n = x.movePointRight(mc!!.precision shl 1)
            .toBigInteger()

        val bits = n.bitLength() + 1 shr 1
        var ix = n.shiftRight(bits)
        var ixPrev: BigInteger

        do {
          ixPrev = ix
          ix = ix.add(n.divide(ix)).shiftRight(1)
          // Give other threads a chance to work;
          Thread.`yield`()
        } while (ix.compareTo(ixPrev) != 0)

        return BigDecimal(ix, mc!!.precision)
      }
    })
 */

/*
interface Function {
    val numParams: Int
    val isFixedNumParams: Boolean
    /**
     * Evaluation of the function

     * @param params Parameters will be passed by the expression evaluator as a [List] of [Object] values.
     * *
     * @param vmap
     * *
     * @return The function must return a new [Object] value as a computing result.
     */
    @Throws(InkRunTimeException::class)
    fun eval(params: List<Any>, vmap: VariableMap): Any

}
*/