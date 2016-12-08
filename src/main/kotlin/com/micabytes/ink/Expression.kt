package com.micabytes.ink

import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

/**
 * Based on https://github.com/uklimaschewski/EvalEx
 *
 * Creates a new expression instance from an expression string with a given
 * default match context.

 * @param expression         The expression. E.g. `"2.4*sin(3)/(2-4)"` or
 * *                           `"sin(y)>0 & max(z, 3)>3"`
 * *
 * @param defaultMathContext The [MathContext] to use by default.
 */
class Expression (val expression: String, val defaultMathContext: MathContext = MathContext.DECIMAL32)
{
  /// The expression evaluators exception class.
  class ExpressionException(message: String) : RuntimeException(message)
  /// The {@link MathContext} to use for calculations.
  private var mc = defaultMathContext
  /// The cached RPN (Reverse Polish Notation) of the expression.
  private var rpn: List<String>? = null
  /// All defined operators with name and implementation.
  private val operators = TreeMap<String, Operator>(String.CASE_INSENSITIVE_ORDER)

  /* Abstract definition of a supported operator. An operator is defined by its name (pattern), precedence and
   * if it is left- or right associative.
   */
  abstract inner class Operator (val name: String,
                                 val precedence: Int,
                                 val isLeftAssociative: Boolean) {
    abstract fun eval(v1: Any, v2: Any): BigDecimal
  }

  private inner class Tokenizer(input: String) : Iterator<String> {
    var pos = 0
      private set
    private val input: String
    private var previousToken: String? = null

    init {
      this.input = input.trim { it <= ' ' }
    }

    override fun hasNext(): Boolean {
      return pos < input.length
    }

    private fun peekNextChar(): Char {
      if (pos < input.length - 1) {
        return input[pos + 1]
      } else
        return 0.toChar()
    }

    override fun next(): String {
      val token = StringBuilder()
      if (pos >= input.length) {
        previousToken = null
        return ""
      }
      var ch = input[pos]
      while (Character.isSpaceChar(ch) && pos < input.length) {
        ch = input[++pos]
      }
      if (Character.isDigit(ch)) {
        while ((Character.isDigit(ch) || ch == decimalSeparator
            || ch == 'e' || ch == 'E'
            || ch == minusSign && token.length > 0
            && ('e' == token[token.length - 1] || 'E' == token[token.length - 1])
            || ch == '+' && token.length > 0
            && ('e' == token[token.length - 1] || 'E' == token[token.length - 1])) && pos < input.length) {
          token.append(input[pos++])
          ch = if (pos == input.length) 0.toChar() else input[pos]
        }
      } else if (ch == minusSign
          && Character.isDigit(peekNextChar())
          && ("(" == previousToken || "," == previousToken
          || previousToken == null || operators.containsKey(previousToken!!))) {
        token.append(minusSign)
        pos++
        val tk = next()
        if (tk != null) token.append(tk)
      } else if (Character.isLetter(ch) || ch == '_' || ch == '\"') {
        while ((Character.isLetter(ch) || Character.isDigit(ch) || ch == '_' || ch == '.' || ch == '\"') && pos < input.length) {
          token.append(input[pos++])
          ch = if (pos == input.length) 0.toChar() else input[pos]
        }
      } else if (ch == '(' || ch == ')' || ch == ',') {
        token.append(ch)
        pos++
      } else {
        while (!Character.isLetter(ch) && !Character.isDigit(ch)
            && ch != '_' && !Character.isSpaceChar(ch)
            && ch != '(' && ch != ')' && ch != ','
            && pos < input.length) {
          token.append(input[pos])
          pos++
          ch = if (pos == input.length) 0.toChar() else input[pos]
          if (ch == minusSign) {
            break
          }
        }
        if (!operators.containsKey(token.toString())) {
          throw ExpressionException("Unknown operator '" + token + "' at position " + (pos - token.length + 1))
        }
      }
      previousToken = token.toString()
      return previousToken as String
    }

    /*override fun remove() {
      throw ExpressionException("remove() not supported")
    }*/

  }

  init {
    addOperator(object : Operator("+", 20, true) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return (v1 as BigDecimal).add(v2 as BigDecimal, mc!!)
      }
    })
    addOperator(object : Operator("-", 20, true) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        var b1: BigDecimal? = null
        var b2: BigDecimal? = null
        if (v1 is BigDecimal) b1 = v1
        if (v2 is BigDecimal) b2 = v2
        if (v1 is Int) b1 = BigDecimal.valueOf(v1.toLong())
        if (v2 is Int) b2 = BigDecimal.valueOf(v2.toLong())
        if (b1 == null)
          throw ExpressionException("Object v1 " + v1.toString() + " is not a valid number")
        if (b2 == null)
          throw ExpressionException("Object v2 " + v2.toString() + " is not a valid number")
        return b1.subtract(b2, mc!!)
      }
    })
    addOperator(object : Operator("*", 30, true) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return (v1 as BigDecimal).multiply(v2 as BigDecimal, mc!!)
      }
    })
    addOperator(object : Operator("/", 30, true) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return (v1 as BigDecimal).divide(v2 as BigDecimal, mc!!)
      }
    })
    addOperator(object : Operator("%", 30, true) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return (v1 as BigDecimal).remainder(v2 as BigDecimal, mc)
      }
    })
    addOperator(object : Operator("&&", 4, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        val b1: Boolean
        val b2: Boolean
        if (v1 is Boolean)
          b1 = v1
        else
          b1 = v1 as BigDecimal != BigDecimal.ZERO
        if (v2 is Boolean)
          b2 = v2
        else
          b2 = v2 as BigDecimal != BigDecimal.ZERO
        return if (b1 && b2) BigDecimal.ONE else BigDecimal.ZERO
      }
    })

    addOperator(object : Operator("||", 2, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        val b1: Boolean
        val b2: Boolean
        if (v1 is Boolean)
          b1 = v1
        else
          b1 = v1 as BigDecimal != BigDecimal.ZERO
        if (v2 is Boolean)
          b2 = v2
        else
          b2 = v2 as BigDecimal != BigDecimal.ZERO
        return if (b1 || b2) BigDecimal.ONE else BigDecimal.ZERO
      }
    })

    addOperator(object : Operator(">", 10, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return if ((v1 as BigDecimal).compareTo(v2 as BigDecimal) == 1) BigDecimal.ONE else BigDecimal.ZERO
      }
    })

    addOperator(object : Operator(">=", 10, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return if ((v1 as BigDecimal).compareTo(v2 as BigDecimal) >= 0) BigDecimal.ONE else BigDecimal.ZERO
      }
    })

    addOperator(object : Operator("<", 10, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return if ((v1 as BigDecimal).compareTo(v2 as BigDecimal) == -1)
          BigDecimal.ONE
        else
          BigDecimal.ZERO
      }
    })

    addOperator(object : Operator("<=", 10, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return if ((v1 as BigDecimal).compareTo(v2 as BigDecimal) <= 0) BigDecimal.ONE else BigDecimal.ZERO
      }
    })

    addOperator(object : Operator("=", 7, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        if (v1 is BigDecimal && v2 is BigDecimal)
          return if (v1.compareTo(v2) == 0) BigDecimal.ONE else BigDecimal.ZERO
        if (v1 is String && v2 is String)
          return if (stripStringParameter(v1) == stripStringParameter(v2)) BigDecimal.ONE else BigDecimal.ZERO
        throw ExpressionException("Both sides of an expression must be either numerical or a string")
      }
    })
    addOperator(object : Operator("==", 7, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return operators.get("=")!!.eval(v1, v2)
      }
    })

    addOperator(object : Operator("!=", 7, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return if (operators.get("=")!!.eval(v1, v2) === BigDecimal.ONE) BigDecimal.ZERO else BigDecimal.ONE
      }
    })
    addOperator(object : Operator("<>", 7, false) {
      override fun eval(v1: Any, v2: Any): BigDecimal {
        return operators.get("!=")!!.eval(v1, v2)
      }
    })

    /*
addFunction(new KnotFunction("NOT", 1) {
  @Override
  public BigDecimal eval(List<BigDecimal> parameters) {
    boolean zero = parameters.get(0).compareTo(BigDecimal.ZERO) == 0;
    return zero ? BigDecimal.ONE : BigDecimal.ZERO;
  }
});

addFunction(new KnotFunction("RANDOM", 0) {
  @Override
  public BigDecimal eval(List<BigDecimal> parameters) {
    double d = Math.random();
    return new BigDecimal(d, mc);
  }
});
*/

  }

  private fun isNumber(st: String): Boolean {
    if (st[0] == minusSign && st.length == 1) return false
    if (st[0] == '+' && st.length == 1) return false
    if (st[0] == 'e' || st[0] == 'E') return false
    for (ch in st.toCharArray()) {
      if (!Character.isDigit(ch) && ch != minusSign
          && ch != decimalSeparator
          && ch != 'e' && ch != 'E' && ch != '+')
        return false
    }
    return true
  }

  private fun isStringParameter(st: String): Boolean {
    if (st.startsWith("\"") && st.endsWith("\""))
      return true
    if (st.startsWith("\'") && st.endsWith("\'"))
      return true
    return false
  }

  private fun stripStringParameter(st: String): String {
    if (st.startsWith("\"") && st.endsWith("\""))
      return st.substring(1, st.length - 1)
    if (st.startsWith("\'") && st.endsWith("\'"))
      return st.substring(1, st.length - 1)
    return st
  }

  @Throws(InkRunTimeException::class)
  private fun shuntingYard(expression: String, story: VariableMap): List<String> {
    val outputQueue = ArrayList<String>()
    val stack = Stack<String>()
    val tokenizer = Tokenizer(expression)
    var lastFunction: String? = null
    var previousToken: String? = null
    while (tokenizer.hasNext()) {
      val token : String = tokenizer.next()
      if (isNumber(token)) {
        outputQueue.add(token)
      } else if (isStringParameter(token)) {
        outputQueue.add(token)
      } else if (story.hasVariable(token)) {
        outputQueue.add(token)
      } else if (story.hasFunction(token)) {
        stack.push(token)
        lastFunction = token
      } else if (story.checkObject(token)) {
        stack.push(token)
        lastFunction = token
      } else if (Character.isLetter(token[0])) {
        stack.push(token)
      } else if ("," == token) {
        while (!stack.isEmpty() && "(" != stack.peek()) {
          outputQueue.add(stack.pop())
        }
        if (stack.isEmpty()) {
          throw ExpressionException("Parse error for function '"
              + lastFunction + "'")
        }
      } else if (operators.containsKey(token)) {
        val o1 = operators.get(token)
        var token2: String? = if (stack.isEmpty()) null else stack.peek()
        while (token2 != null &&
            operators.containsKey(token2)
            && (o1!!.isLeftAssociative && o1.precedence <= operators.get(token2)!!.precedence || o1.precedence < operators.get(token2)!!.precedence)) {
          outputQueue.add(stack.pop())
          token2 = if (stack.isEmpty()) null else stack.peek()
        }
        stack.push(token)
      } else if ("(" == token) {
        if (previousToken != null) {
          if (isNumber(previousToken)) {
            throw ExpressionException(
                "Missing operator at character position " + tokenizer.pos)
          }
          // if the ( is preceded by a valid function, then it
          // denotes the start of a parameter list
          if (story.hasFunction(previousToken) || story.checkObject(previousToken)) {
            outputQueue.add(token)
          }
        }
        stack.push(token)
      } else if (")" == token) {
        while (!stack.isEmpty() && "(" != stack.peek()) {
          outputQueue.add(stack.pop())
        }
        if (stack.isEmpty()) {
          throw InkRunTimeException("Mismatched parentheses")
        }
        stack.pop()
        if (!stack.isEmpty() && (story.hasFunction(stack.peek()) || story.checkObject(stack.peek()))) {
          outputQueue.add(stack.pop())
        }
      }
      previousToken = token
    }
    while (!stack.isEmpty()) {
      val element = stack.pop()
      if ("(" == element || ")" == element) {
        throw InkRunTimeException("Mismatched parentheses")
      }
      if (!operators.containsKey(element)) {
        throw InkRunTimeException("Unknown operator or function: " + element)
      }
      outputQueue.add(element)
    }
    return outputQueue
  }

  /**
   * Evaluates the expression.

   * @return The result of the expression.
   */
  @Throws(InkRunTimeException::class)
  fun eval(story: VariableMap): Any {
    val stack = Stack<Any>()
    for (token in getRPN(story)) {
      if (operators.containsKey(token)) {
        val v1 = stack.pop()
        val v2 = stack.pop()
        stack.push(operators.get(token)!!.eval(v2, v1))
      } else if (story.hasVariable(token)) {
        val obj = story.getValue(token)
        if (obj is Boolean) {
          if (obj)
            stack.push(BigDecimal.ONE)
          else
            stack.push(BigDecimal.ZERO)
        } else if (obj is BigDecimal) {
          stack.push(obj.round(mc))
        } else {
          stack.push(obj)
        }
      } else if (story.hasFunction(token)) {
        val f = story.getFunction(token)
        val p = ArrayList<Any>(if (f.isFixedNumParams) f.numParams else 0)
        // pop parameters off the stack until we hit the start of
        // this function's parameter list
        while (!stack.isEmpty() && stack.peek() !== PARAMS_START) {
          val param = stack.pop()
          if (param is String)
            p.add(0, stripStringParameter(param))
          else
            p.add(0, param)
        }
        if (stack.peek() === PARAMS_START) {
          stack.pop()
        }
        if (f.isFixedNumParams && p.size != f.numParams) {
          throw ExpressionException("Function " + token + " expected " + f.numParams + " parameters, got " + p.size)
        }
        val fResult = f.eval(p, story)
        stack.push(fResult)
      } else if (story.checkObject(token)) {
        val vr = token.substring(0, token.indexOf("."))
        val function = token.substring(token.indexOf(".") + 1)
        val vl = story.getValue(vr)
        if (vl == null) {
          stack.push("")
        } else {
          val p = ArrayList<Any>()
          // pop parameters off the stack until we hit the start of  this function's parameter list
          while (!stack.isEmpty() && stack.peek() !== PARAMS_START) {
            val obj = stack.pop()
            if (obj != null) {
              if (obj is String)
                p.add(0, stripStringParameter(obj))
              else
                p.add(0, obj)
            }
          }
          if (stack.peek() === PARAMS_START) {
            stack.pop()
          }
          val paramTypes = arrayOfNulls<Class<*>>(p.size)
          val params = arrayOfNulls<Any>(p.size)
          for (i in p.indices) {
            paramTypes[i] = p[i].javaClass
            params[i] = p[i]
          }
          val valClass = vl.javaClass
          try {
            val m = valClass.getMethod(function, *paramTypes)
            var fResult = m.invoke(vl, *params)
            if (fResult is Int)
              fResult = BigDecimal.valueOf((fResult as Int).toLong())
            else if (fResult is Float)
              fResult = BigDecimal.valueOf((fResult as Float).toDouble())
            else if (fResult is Double)
              fResult = BigDecimal.valueOf(fResult as Double)
            stack.push(fResult)
          } catch (e: NoSuchMethodException) {
            var errMsg = "Could not identify a method " + function + " on variable " + vr + " (" + vr + ", " + valClass.getName() + ") with the parameters:"
            for (i in paramTypes.indices)
              errMsg += " " + paramTypes[i]!!.getName()
            errMsg += ". " + story.debugInfo()
            throw InkRunTimeException(errMsg, e)
          } catch (e: InvocationTargetException) {
            var errMsg = "Could not invoke a method " + function + " on variable " + vr + " (" + vr + ", " + valClass.getName() + ") with the parameters:"
            for (i in paramTypes.indices)
              errMsg += " " + paramTypes[i]!!.getName()
            errMsg += ". " + story.debugInfo()
            throw InkRunTimeException(errMsg, e)
          } catch (e: IllegalAccessException) {
            var errMsg = "Could not access a method " + function + " on variable " + vr + " (" + vr + ", " + valClass.getName() + ") with the parameters:"
            for (i in paramTypes.indices)
              errMsg += " " + paramTypes[i]!!.getName()
            errMsg += ". " + story.debugInfo()
            throw InkRunTimeException(errMsg, e)
          }

        }
      } else if ("(" == token) {
        stack.push(PARAMS_START)
      } else {
        if (isNumber(token))
          stack.push(BigDecimal(token, mc))
        else
          stack.push(token)
      }
    }
    val obj = stack.pop()
    if (obj is BigDecimal)
      return obj.stripTrailingZeros()
    if (obj is String) {
      if (isStringParameter(obj))
        return stripStringParameter(obj)
      return obj
    }
    return obj
  }

  /**
   * Sets the precision for expression evaluation.

   * @param precision The new precision.
   * *
   * @return The expression, allows to chain methods.
   */
  fun setPrecision(precision: Int): Expression {
    this.mc = MathContext(precision)
    return this
  }

  /**
   * Sets the rounding mode for expression evaluation.

   * @param roundingMode The new rounding mode.
   * *
   * @return The expression, allows to chain methods.
   */
  fun setRoundingMode(roundingMode: RoundingMode): Expression {
    this.mc = MathContext(mc!!.precision, roundingMode)
    return this
  }

  /**
   * Adds an operator to the list of supported operators.

   * @param operator The operator to add.
   * *
   * @return The previous operator with that name, or `null` if
   * * there was none.
   */
  fun addOperator(operator: Operator): Operator {
    return operators.put(operator.name, operator)!!
  }

  /**
   * Adds a function to the list of supported functions

   * @param function The function to add.
   * *
   * @return The previous operator with that name, or `null` if
   * * there was none.
   * *
   * public KnotFunction addFunction(KnotFunction function) {
   * return story.functions.put(function.getName(), function);
   * }
   */

  /**
   * Cached access to the RPN notation of this expression, ensures only one
   * calculation of the RPN per expression instance. If no cached instance
   * exists, a new one will be created and put to the cache.

   * @return The cached RPN instance.
   * *
   * @param story
   */
  @Throws(InkRunTimeException::class)
  private fun getRPN(story: VariableMap): List<String> {
    if (rpn == null) {
      rpn = shuntingYard(this.expression, story)
      validate(rpn!!, story)
    }
    return rpn as List<String>
  }

  /**
   * Check that the expression have enough numbers and variables to fit the
   * requirements of the operators and functions, also check
   * for only 1 result stored at the end of the evaluation.
   */
  private fun validate(rpn: List<String>, story: VariableMap) {
    /*-
* Thanks to Norman Ramsey:
* http://http://stackoverflow.com/questions/789847/postfix-notation-validation
*/
    var counter = 0
    val params = Stack<Int>()
    for (token in rpn) {
      if ("(" == token) {
        // is this a nested function call?
        if (!params.isEmpty()) {
          // increment the current function's param count
          // (the return of the nested function call
          // will be a parameter for the current function)
          params[params.size - 1] = params.peek() + 1
        }
        // start a new parameter count
        params.push(0)
      } else if (!params.isEmpty()) {
        if (story.hasFunction(token) || story.checkObject(token)) {
          // remove the parameters and the ( from the counter
          counter -= params.pop() + 1
        } else {
          // increment the current function's param count
          params[params.size - 1] = params.peek() + 1
        }
      } else if (operators.containsKey(token)) {
        //we only have binary operators
        counter -= 2
      }
      if (counter < 0) {
        throw ExpressionException("Too many operators or functions at: " + token)
      }
      counter++
    }
    if (counter > 1) {
      throw ExpressionException("Too many numbers or variables")
    } else if (counter < 1) {
      throw ExpressionException("Empty expression")
    }
  }

  companion object {
    /// What character to use for decimal separators.
    private val decimalSeparator = '.'
    /// What character to use for minus sign (negative values).
    private val minusSign = '-'
    /// The BigDecimal representation of the left parenthesis, used for parsing varying numbers of function parameters.
    private val PARAMS_START = BigDecimal(0)
  }

}
/**
 * Creates a new expression instance from an expression string with a given
 * default match context of [MathContext.DECIMAL32].

 * @param expression The expression. E.g. `"2.4*sin(3)/(2-4)"` or
 * *                   `"sin(y)>0 & max(z, 3)>3"`
 */
