package com.micabytes.ink

import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.util.ArrayList
import java.util.Collections
import java.util.Locale
import java.util.Stack
import java.util.TreeMap
import jdk.nashorn.internal.objects.NativeArray.pop



/**
 * Based on https://github.com/uklimaschewski/EvalEx
 * Creates a new expression instance from an expression string with a given default match context.
 * @param expression         The expression. E.g. `"2.4*sin(3)/(2-4)"` or `"sin(y)>0 & max(z, 3)>3"`
 * @param defaultMathContext The [MathContext] to use by default
 */
class Expression constructor(val originalExpression: String,
                             val defaultMathContext: MathContext = MathContext.DECIMAL32) {
  /// The expression evaluators exception class.
  class ExpressionException(message: String) : RuntimeException(message)

  /// The [MathContext] to use for calculations.
  private var mc = defaultMathContext
  /// The cached RPN (Reverse Polish Notation) of the expression.
  private var rpn: List<String>? = null
  ///The characters (other than letters and digits) allowed as the first character in a variable.
  private val firstVarChars: String = "_"
  /// The characters (other than letters and digits) allowed as the second or subsequent characters in a variable.
  private val varChars: String = "_."
  /// All defined operators with name and implementation.
  private val operators = TreeMap<String, Operator>(String.CASE_INSENSITIVE_ORDER)
  /// The current infix expression, with optional variable substitutions.
  var expression: String = originalExpression
    private set

  /**
   * Expression tokenizer that allows to iterate over a [String] expression token by token. Blank characters will be
   * skipped.
   */
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
      } else {
        return 0.toChar()
      }
    }

    override fun next(): String {
      val token = StringBuilder()
      if (pos >= input.length) {
        previousToken = null
        return ""
      }
      var ch = input[pos]
      while (Character.isWhitespace(ch) && pos < input.length) {
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
          ch = if (pos == input.length) '0' else input[pos]
        }
      } else if (ch == minusSign
          && Character.isDigit(peekNextChar())
          && ("(" == previousToken
          || "," == previousToken
          || previousToken == null
          || operators.containsKey(previousToken!!))) {
        token.append(minusSign)
        pos++
        token.append(next())
      } else if (Character.isLetter(ch) || firstVarChars.indexOf(ch) >= 0 || ch == '_' || ch == '\"') {
        while ((Character.isLetter(ch)
            || Character.isDigit(ch)
            || varChars.indexOf(ch) >= 0
            || ch == '_' || ch == '.' || ch == '\"'
            || token.length == 0 && firstVarChars.indexOf(ch) >= 0) && pos < input.length) {
          token.append(input[pos++])
          ch = if (pos == input.length) '0' else input[pos]
        }
      } else if (ch == '(' || ch == ')' || ch == ',') {
        token.append(ch)
        pos++
      } else {
        while (!Character.isLetter(ch) && !Character.isDigit(ch)
            && firstVarChars.indexOf(ch) < 0 && !Character.isWhitespace(ch)
            && ch != '(' && ch != ')' && ch != ','
            && pos < input.length) {
          token.append(input[pos])
          pos++
          ch = if (pos == input.length) '0' else input[pos]
          if (ch == minusSign) {
            break
          }
        }
        if (!operators.containsKey(token.toString())) {
          throw ExpressionException("Unknown operator '$token' at position " + (pos - token.length + 1))
        }
      }
      previousToken = token.toString()
      return previousToken as String
    }

  }

  init {
    this.mc = defaultMathContext
    this.expression = originalExpression
    addOperator(object : Operator("+", 20, true) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return v1.add(v2, mc)
      }
    })
    addOperator(object : Operator("-", 20, true) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return v1.subtract(v2, mc)
      }
    })
    addOperator(object : Operator("*", 30, true) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return v1.multiply(v2, mc)
      }
    })
    addOperator(object : Operator("/", 30, true) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return v1.divide(v2, mc)
      }
    })
    addOperator(object : Operator("%", 30, true) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return v1.remainder(v2, mc)
      }
    })
    addOperator(object : Operator("^", 40, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        var v2 = v2
        val signOf2 = v2.signum()
        val dn1 = v1.toDouble()
        v2 = v2.multiply(BigDecimal(signOf2)) // n2 is now positive
        val remainderOf2 = v2.remainder(BigDecimal.ONE)
        val n2IntPart = v2.subtract(remainderOf2)
        val intPow = v1.pow(n2IntPart.intValueExact(), mc!!)
        val doublePow = BigDecimal(Math.pow(dn1,
            remainderOf2.toDouble()))
        var result = intPow.multiply(doublePow, mc!!)
        if (signOf2 == -1) {
          result = BigDecimal.ONE.divide(result, mc!!.precision,
              RoundingMode.HALF_UP)
        }
        return result
      }
    })
    addOperator(object : Operator("&&", 4, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        val b1 = v1 != BigDecimal.ZERO
        val b2 = v2 != BigDecimal.ZERO
        return if (b1 && b2) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("||", 2, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        val b1 = v1 != BigDecimal.ZERO
        val b2 = v2 != BigDecimal.ZERO
        return if (b1 || b2) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator(">", 10, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return if (v1.compareTo(v2) == 1) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator(">=", 10, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return if (v1.compareTo(v2) >= 0) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("<", 10, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return if (v1.compareTo(v2) == -1)
          BigDecimal.ONE
        else
          BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("<=", 10, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return if (v1.compareTo(v2) <= 0) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("=", 7, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return if (v1.compareTo(v2) == 0) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("==", 7, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return operators["="]!!.eval(v1, v2)
      }
    })
    addOperator(object : Operator("!=", 7, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return if (v1.compareTo(v2) != 0) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("<>", 7, false) {
      override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
        return operators["!="]!!.eval(v1, v2)
      }
    })
    /*
    addFunction(object : Function("NOT", 1) {
      override fun eval(parameters: List<Any>): Any {
        val param = parameters[0]
        if (param is Boolean)
          return if (param) BigDecimal.ZERO else BigDecimal.ONE
        if (param is BigDecimal) {
          val zero = param.compareTo(BigDecimal.ZERO) == 0
          return if (zero) BigDecimal.ONE else BigDecimal.ZERO
        }
        return BigDecimal.ZERO
      }
    })
    addLazyFunction(object : LazyFunction("IF", 3) {
      override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
        val isTrue = lazyParams[0].eval() != BigDecimal.ZERO
        return if (isTrue) lazyParams[1] else lazyParams[2]
      }
    })
    addFunction(object : Function("RANDOM", 0) {
      override fun eval(parameters: List<Any>): Any {
        val d = Math.random()
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("MAX", -1) {
      override fun eval(parameters: List<Any>): Any {
        if (parameters.isEmpty()) {
          throw ExpressionException("MAX requires at least one parameter")
        }
        var max: BigDecimal? = null
        for (parameter in parameters) {
          when (parameter) {
            is BigDecimal -> {
              if (max == null || parameter > max) {
                max = parameter
              }
            }
            else -> {
              throw ExpressionException("MAX requires all parameters to be BigDecimal")
            }
          }
        }
        return max!!
      }
    })
    addFunction(object : Function("MIN", -1) {
      override fun eval(parameters: List<Any>): Any {
        if (parameters.isEmpty()) {
          throw ExpressionException("MIN requires at least one parameter")
        }
        var min: BigDecimal? = null
        for (parameter in parameters) {
          when (parameter) {
            is BigDecimal -> {
              if (min == null || parameter > min) {
                min = parameter
              }
            }
            else -> {
              throw ExpressionException("MIN requires all parameters to be BigDecimal")
            }
          }
        }
        return min!!
      }
    })
    addFunction(object : Function("ABS", 1) {
      override fun eval(parameters: List<Any>): Any {
        return ((parameters[0] as BigDecimal).abs(mc) as BigDecimal)
      }
    })
    addFunction(object : Function("ROUND", 2) {
      override fun eval(parameters: List<Any>): Any {
        val toRound = parameters[0] as BigDecimal
        val precision = (parameters[1] as BigDecimal).toInt()
        return toRound.setScale(precision, mc!!.roundingMode)
      }
    })
    addFunction(object : Function("FLOOR", 1) {
      override fun eval(parameters: List<Any>): Any {
        val toRound = parameters[0] as BigDecimal
        return toRound.setScale(0, RoundingMode.FLOOR)
      }
    })
    addFunction(object : Function("CEIL", 1) {
      override fun eval(parameters: List<Any>): Any {
        val toRound = parameters[0] as BigDecimal
        return toRound.setScale(0, RoundingMode.CEILING)
      }
    })
    */
    //values.put("e", e)
    //values.put("PI", PI)
    //values.put("TRUE", BigDecimal.ONE)
    //values.put("FALSE", BigDecimal.ZERO)
  }

  @Throws(InkRunTimeException::class)
  private fun shuntingYard(expression: String, vMap: VariableMap): List<String> {
    val outputQueue = ArrayList<String>()
    val stack = Stack<String>()
    val tokenizer = Tokenizer(expression)
    var lastFunction: String? = null
    var previousToken: String? = null
    while (tokenizer.hasNext()) {
      val token = tokenizer.next()
      if (isNumber(token)) {
        outputQueue.add(token)
      } else if (isStringParameter(token)) {
        outputQueue.add(token)
      } else if (vMap.hasVariable(token)) {
        outputQueue.add(token)
      } else if (vMap.hasFunction(token)) {
        stack.push(token)
        lastFunction = token
      } else if (vMap.checkObject(token)) {
        stack.push(token)
        lastFunction = token
      } else if (Character.isLetter(token[0])) {
        stack.push(token)
      } else if ("," == token) {
        if (operators.containsKey(previousToken)) {
          throw ExpressionException("Missing parameter(s) for operator " + previousToken + " at character position " + (tokenizer.pos - 1 - previousToken!!.length))
        }
        while (!stack.isEmpty() && "(" != stack.peek()) {
          outputQueue.add(stack.pop())
        }
        if (stack.isEmpty()) {
          throw ExpressionException("Parse error for function '" + lastFunction + "'")
        }
      } else if (operators.containsKey(token)) {
        if ("," == previousToken || "(" == previousToken) {
          throw ExpressionException("Missing parameter(s) for operator " + token + " at character position " + (tokenizer.pos - token.length))
        }
        val o1: Operator = operators[token]!!
        var token2: String? = if (stack.isEmpty()) null else stack.peek()
        while (token2 != null
            && operators.containsKey(token2)
            && (o1.isLeftAssoc && o1.precedence <= operators[token2]!!.precedence || o1.precedence < operators[token2]!!.precedence)) {
          outputQueue.add(stack.pop())
          token2 = if (stack.isEmpty()) null else stack.peek()
        }
        stack.push(token)
      } else if ("(" == token) {
        if (previousToken != null) {
          if (isNumber(previousToken)) {
            throw ExpressionException("Missing operator at character position " + tokenizer.pos)
          }
          // if the ( is preceded by a valid function, then it denotes the start of a parameter list
          if (vMap.hasFunction(previousToken) || vMap.checkObject(previousToken)) {
            outputQueue.add(token)
          }
        }
        stack.push(token)
      } else if (")" == token) {
        if (operators.containsKey(previousToken)) {
          throw ExpressionException("Missing parameter(s) for operator " + previousToken + " at character position " + (tokenizer.pos - 1 - previousToken!!.length))
        }
        while (!stack.isEmpty() && "(" != stack.peek()) {
          outputQueue.add(stack.pop())
        }
        if (stack.isEmpty()) {
          throw ExpressionException("Mismatched parentheses")
        }
        stack.pop()
        if (!stack.isEmpty() && (vMap.hasFunction(stack.peek()) || vMap.checkObject(stack.peek())) ) {
          outputQueue.add(stack.pop())
        }
      }
      previousToken = token
    }
    while (!stack.isEmpty()) {
      val element = stack.pop()
      if ("(" == element || ")" == element) {
        throw ExpressionException("Mismatched parentheses")
      }
      if (!operators.containsKey(element)) {
        throw ExpressionException("Unknown operator or function: " + element)
      }
      outputQueue.add(element)
    }
    return outputQueue
  }

  @Throws(InkRunTimeException::class)
  fun eval(vMap: VariableMap): Any {
    val stack = Stack<Any>()
    for (token in getRPN(vMap)) {
      if (isNumber(token))
        stack.push(BigDecimal(token, mc))
      else if (operators.containsKey(token)) {
        val v1 = stack.pop()
        val v2 = stack.pop()
        stack.push(operators.get(token)!!.eval(v2 as BigDecimal, v1 as BigDecimal))
        //val number = object : LazyNumber {
        //  override fun eval(): BigDecimal {
        //    return operators[token]!!.eval(v2.eval(), v1.eval())
        //  }
        //}
        //stack.push(number)
      } else if (vMap.hasVariable(token)) {
        val obj = vMap.getValue(token)
        when (obj) {
          is BigDecimal -> {
            stack.push(obj.round(mc))
          }
          else -> {
            stack.push(obj)
          }
        }
      } else if (vMap.hasFunction(token)) {
        val f = vMap.getFunction(token)
        val p = ArrayList<Any>(if (f.isFixedNumParams()) f.numParams() else 0)
        // pop parameters off the stack until we hit the start of  this function's parameter list
        while (!stack.isEmpty() && stack.peek() !== Expression.PARAMS_START) {
          val param = stack.pop()
          if (param is String)
            p.add(0, stripStringParameter(param))
          else
            p.add(0, param)
        }
        if (stack.peek() === Expression.PARAMS_START) {
          stack.pop()
        }
        if (f.isFixedNumParams() && p.size != f.numParams()) {
          throw ExpressionException("Function " + token + " expected " + f.numParams() + " parameters, got " + p.size)
        }
        stack.push(f.eval(p, vMap))
      } else if (vMap.checkObject(token)) {
        val vr = token.substring(0, token.indexOf("."))
        val function = token.substring(token.indexOf(".") + 1)
        val vl = vMap.getValue(vr)
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
            when (fResult) {
              is Boolean ->
                fResult = if (fResult) BigDecimal.ONE else BigDecimal.ZERO
              is Int ->
                fResult = BigDecimal(fResult)
              is Float ->
                fResult = BigDecimal(fResult.toDouble())
              is Double ->
                fResult = BigDecimal(fResult)
            }
            stack.push(fResult)
          } catch (e: NoSuchMethodException) {
            var errMsg = "Could not identify a method " + function + " on variable " + vr + " (" + vr + ", " + valClass.getName() + ") with the parameters:"
            for (i in paramTypes.indices)
              errMsg += " " + paramTypes[i]!!.getName()
            errMsg += ". " + vMap.debugInfo()
            throw InkRunTimeException(errMsg, e)
          } catch (e: InvocationTargetException) {
            var errMsg = "Could not invoke a method " + function + " on variable " + vr + " (" + vr + ", " + valClass.getName() + ") with the parameters:"
            for (i in paramTypes.indices)
              errMsg += " " + paramTypes[i]!!.getName()
            errMsg += ". " + vMap.debugInfo()
            throw InkRunTimeException(errMsg, e)
          } catch (e: IllegalAccessException) {
            var errMsg = "Could not access a method " + function + " on variable " + vr + " (" + vr + ", " + valClass.getName() + ") with the parameters:"
            for (i in paramTypes.indices)
              errMsg += " " + paramTypes[i]!!.getName()
            errMsg += ". " + vMap.debugInfo()
            throw InkRunTimeException(errMsg, e)
          }
        }
      } else if ("(" == token) {
        stack.push(PARAMS_START)
      } else {
        stack.push(token)
        //stack.push(object : LazyNumber {
        //  override fun eval(): BigDecimal {
        //    return BigDecimal(token, mc!!)
        //  }
        //})
      }
    }
    val obj = stack.pop()
    when (obj) {
      is BigDecimal ->
        return obj.stripTrailingZeros()
      is String ->
        if (isStringParameter(obj)) return stripStringParameter(obj)
    }
    return obj
  }

  fun setPrecision(precision: Int): Expression {
    this.mc = MathContext(precision)
    return this
  }

  fun setRoundingMode(roundingMode: RoundingMode): Expression {
    this.mc = MathContext(mc!!.precision, roundingMode)
    return this
  }

  //fun setFirstVariableCharacters(chars: String): Expression {
  //  this.firstVarChars = chars
  //  return this
  //}

  //fun setVariableCharacters(chars: String): Expression {
  //  this.varChars = chars
  //  return this
  //}

  fun addOperator(oper: Operator) {
    operators.put(oper.oper, oper)
  }

  /*
  fun addFunction(function: Function) {
    functions.put(function.name, function)
  }

  fun addLazyFunction(function: LazyFunction) {
    functions.put(function.name, function)
  }

  fun setVariable(variable: String, value: BigDecimal): Expression {
    values.put(variable, value)
    return this
  }

  fun setVariable(variable: String, value: String): Expression {
    if (isNumber(value))
      values.put(variable, BigDecimal(value))
    else {
      expression = expression!!.replace("(?i)\\b$variable\\b".toRegex(), "(" + value + ")")
      rpn = null
    }
    return this
  }

  fun with(variable: String, value: BigDecimal): Expression {
    return setVariable(variable, value)
  }

  fun and(variable: String, value: String): Expression {
    return setVariable(variable, value)
  }

  fun and(variable: String, value: BigDecimal): Expression {
    return setVariable(variable, value)
  }

  fun with(variable: String, value: String): Expression {
    return setVariable(variable, value)
  }

  val expressionTokenizer: Iterator<String>
    get() = Tokenizer(expression!!)
    */

  private fun getRPN(vMap: VariableMap): List<String> {
    if (rpn == null) {
      rpn = shuntingYard(expression, vMap)
      validate(rpn!!, vMap)
    }
    return rpn!!
  }

  private fun validate(rpn: List<String>, vMap: VariableMap) {
    val stack = Stack<Int>()
    stack.push(0)
    for (token in rpn) {
      if (operators.containsKey(token)) {
        if (stack.peek() < 2) {
          throw ExpressionException("Missing parameter(s) for operator " + token)
        }
        // pop the operator's 2 parameters and add the result
        stack[stack.size - 1] = stack.peek() - 2 + 1
      } else if (vMap.hasVariable(token)) {
        stack[stack.size - 1] = stack.peek() + 1
      } else if (vMap.hasFunction(token.toUpperCase(Locale.ROOT))) {
        val f = vMap.getFunction(token.toUpperCase(Locale.ROOT))!!
        val numParams = stack.pop()
        if (f.isFixedNumParams() && numParams != f.numParams()) {
          throw ExpressionException("Function " + token + " expected " + f.numParams() + " parameters, got " + numParams)
        }
        if (stack.size <= 0) {
          throw ExpressionException("Too many function calls, maximum scope exceeded")
        }
        // push the result of the function
        stack[stack.size - 1] = stack.peek() + 1
      } else if (vMap.checkObject(token)) {
        // TODO: Verify this works
        val numParams = stack.pop()
        if (stack.size <= 0) {
          throw ExpressionException("Too many function calls, maximum scope exceeded")
        }        // push the result of the function
        stack.set(stack.size - 1, stack.peek() + 1)
      } else if ("(" == token) {
        stack.push(0)
      } else {
        stack[stack.size - 1] = stack.peek() + 1
      }
    }
    if (stack.size > 1) {
      throw ExpressionException("Too many unhandled function parameter lists")
    } else if (stack.peek() > 1) {
      throw ExpressionException("Too many numbers or values")
    } else if (stack.peek() < 1) {
      throw ExpressionException("Empty expression")
    }
  }

  /*
  fun toRPN(story: VariableMap): String {
    val result = StringBuilder()
    for (st in getRPN(story)) {
      if (result.length != 0)
        result.append(" ")
      result.append(st)
    }
    return result.toString()
  }

  val declaredVariables: Set<String>
    get() = Collections.unmodifiableSet(values.keys)

  val declaredOperators: Set<String>
    get() = Collections.unmodifiableSet(operators.keys)

  val declaredFunctions: Set<String>
    get() = Collections.unmodifiableSet(functions.keys)

  val usedVariables: List<String>
    get() {
      val result = ArrayList<String>()
      val tokenizer = Tokenizer(expression)
      while (tokenizer.hasNext()) {
        val token = tokenizer.next()
        if (functions.containsKey(token) || operators.containsKey(token)
            || token == "(" || token == ")"
            || token == "," || isNumber(token)
            || token == "PI" || token == "e"
            || token == "TRUE" || token == "FALSE") {
          continue
        }
        result.add(token)
      }
      return result
    }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false
    val that = o as Expression?
    if (this.expression == null) {
      return that!!.expression == null
    } else {
      return this.expression == that!!.expression
    }
  }

  override fun hashCode(): Int {
    return if (this.expression == null) 0 else this.expression!!.hashCode()
  }
  */

  override fun toString(): String {
    return this.expression
  }

  companion object {
    val TRUE = "TRUE"
    /// Definition of PI as a constant, can be used in expressions as variable.
    val PI = BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679")
    /// Definition of e: "Euler's number" as a constant, can be used in expressions as variable.
    val e = BigDecimal("2.71828182845904523536028747135266249775724709369995957496696762772407663")
    /// What character to use for decimal separators.
    private val decimalSeparator = '.'
    /// What character to use for minus sign (negative values).
    private val minusSign = '-'
    /// The BigDecimal representation of the left parenthesis, used for parsing varying numbers of function parameters.
    private val PARAMS_START = object : LazyNumber {
      override fun eval(): BigDecimal {
        return BigDecimal(0)
      }
    }

    fun isNumber(st: String): Boolean {
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
      return false
    }

    private fun stripStringParameter(st: String): String {
      if (st.startsWith("\"") && st.endsWith("\""))
        return st.substring(1, st.length - 1)
      if (st.startsWith("\'") && st.endsWith("\'"))
        return st.substring(1, st.length - 1)
      return st
    }

  }

}