package com.micabytes.ink

import com.micabytes.ink.util.InkRunTimeException
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
    if (!story.hasValue(variable))
      throw InkRunTimeException("CalculateVariable $variable is not defined in variable expression on line $lineNumber")
    /*
    // TODO: Check that all variants of attributes work
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
    private val FALSE_LC = "false"

    fun getId(parent: Container): String {
      return parent.id + Symbol.DOT + parent.size
    }

    fun evaluate(str: String, variables: VariableMap): Any {
      // TODO: Note that this means that spacing will mess up expressions; needs to be fixed
      var ev: String = ""
      try {
        ev = str
            .replace(AND_WS.toRegex(), " && ")
            .replace(OR_WS.toRegex(), " || ")
            .replace(TRUE_LC.toRegex(), Symbol.TRUE)
            .replace(FALSE_LC.toRegex(), Symbol.FALSE)
        val ex = Expression(ev)
        return ex.eval(variables)
      } catch (e: Expression.ExpressionException) {
        throw InkRunTimeException("Error evaluating expression " + ev + ". " + e.message, e)
      }
    }

    fun isVariableHeader(str: String): Boolean {
      return str.startsWith(VAR_) || str.startsWith(TILDE_)
    }

  }

}
