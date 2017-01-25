package com.micabytes.ink

import java.util.regex.Pattern

class Declaration internal constructor(lineNumber: Int,
                                       decl: String,
                                       parent: Container) :
    Content(getId(parent),
        if (decl.startsWith(VAR_))
          decl.substring(4).trim({ it <= ' ' })
        else
          decl.substring(1).replace(Symbol.RETURN, Symbol.RETURNEQ).trim({ it <= ' ' })
        ,
        parent,
        lineNumber) {

  val isDeclaration = decl.startsWith(VAR_)

  @Throws(InkRunTimeException::class)
  fun evaluate(story: Story) {
    if (isDeclaration)
      declareVariable(story)
    else
      calculate(story)
  }

  @Throws(InkRunTimeException::class)
  private fun declareVariable(story: Story) {
    val tokens = EQ_SPLITTER.split(text)
    if (tokens.size != 2)
      throw InkRunTimeException("Invalid variable declaration. Expected values, values, and/or operators after \'=\'.")
    val variable = tokens[0].trim { it <= ' ' }
    val value = tokens[1].trim { it <= ' ' }
    if (value.startsWith(Symbol.DIVERT)) {
      val directTo = story.getValue(value)
      if (directTo is Container) {
        story.putVariable(variable, directTo)
        return
      }
      else
        throw InkRunTimeException("DeclareVariable $variable declared as equals to an invalid address $value")
    }
    /*
    if (value.toUpperCase(Locale.US) == TRUE_LC)
      story.putVariable(variable, BigDecimal.ONE)
    else if (value == FALSE_LC)
      story.putVariable(variable, java.lang.Boolean.FALSE)
    else if (isInteger(value) || isFloat(value))
      story.putVariable(variable, BigDecimal(value))
    else if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length - 1)
      if (value.contains(Symbol.DIVERT))
        throw InkRunTimeException("Line number$lineNumber: String expressions cannot contain diverts (->)")
      story.putVariable(variable, value)
    } else  else {
    */
    story.putVariable(variable, evaluate(value, story))
  }

  @Throws(InkRunTimeException::class)
  private fun calculate(story: Story) {
    val tokens = EQ_SPLITTER.split(text)
    if (tokens.size == 1) {
      evaluate(tokens[0], story)
      return
    }
    if (tokens.size > 2)
      throw InkRunTimeException("Invalid variable expression. Expected values, values, and operators after \'=\' in line $lineNumber")
    val variable = tokens[0].trim { it <= ' ' }
    val value = tokens[1].trim { it <= ' ' }
    if (!story.hasVariable(variable))
      throw InkRunTimeException("CalculateVariable $variable is not defined in variable expression on line $lineNumber")
    /*
    if (value == TRUE_LC)
      story.putVariable(variable, java.lang.Boolean.TRUE)
    else if (value == FALSE_LC)
      story.putVariable(variable, java.lang.Boolean.FALSE)
    else if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length - 1)
      story.putVariable(variable, value)
    } else if (value.startsWith(Symbol.DIVERT)) {
      val address = value.substring(3).trim({ it <= ' ' })
      val directTo = story.getValue(address)
      if (directTo is Container)
        story.putVariable(variable, directTo)
      else
        throw InkRunTimeException("Declaration $variable declared to equals invalid address $address")
    } else {
    */
    story.putVariable(variable, evaluate(value, story))
  }

  companion object {
    private val VAR_ = "VAR "
    private val TILDE_ = "~ "
    private val EQ_SPLITTER = Pattern.compile("[=]+")
    private val AND_WS = " and "
    private val OR_WS = " or "
    private val TRUE_LC = "true"
    internal val TRUE_UC = "TRUE"
    private val FALSE_LC = "false"
    internal val FALSE_UC = "FALSE"
    //private val RETURN = Symbol.RETURN
    //private val RETURNEQ = "return ="

    fun getId(parent: Container): String {
      return parent.id + InkParser.DOT + parent.size
    }

    fun evaluate(str: String, variables: VariableMap): Any {
      // TODO: Note that this means that spacing will mess up expressions; needs to be fixed
      var ev: String = ""
      try {
        ev = str.replace(AND_WS.toRegex(), " && ").replace(OR_WS.toRegex(), " || ").replace(TRUE_LC.toRegex(), TRUE_UC).replace(FALSE_LC.toRegex(), FALSE_UC)
        val ex = Expression(ev)
        return ex.eval(variables)
      } catch (e: Expression.ExpressionException) {
        throw InkRunTimeException("Error evaluating expression " + ev, e)
      }
    }

    /*
    private fun isInteger(str: String): Boolean {
      try {
        // Slow and dirty solution
        //noinspection ResultOfMethodCallIgnored
        Integer.parseInt(str)
      } catch (ignored: NumberFormatException) {
        return false
      }
      return true
    }

    private fun isFloat(str: String): Boolean {
      try {
        // Slow and dirty solution
        //noinspection ResultOfMethodCallIgnored
        java.lang.Float.parseFloat(str)
      } catch (ignored: NumberFormatException) {
        return false
      }
      return true
    }
    */

    fun isVariableHeader(str: String): Boolean {
      return str.startsWith(VAR_) || str.startsWith(TILDE_)
    }

  }

}
