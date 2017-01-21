package com.micabytes.ink

open class Content(internal val lineNumber: Int,
                   internal open val text: String,
                   internal val parent: Container?) {
  internal open val id : String = if (parent != null) parent.id + InkParser.DOT + parent.indexOf(this) else ""
  internal open val type = ContentType.TEXT
  internal var count: Int = 0

  init {
    parent?.add(this)
  }

  open fun  getText(story: Story): String {
    return StoryText.getText(text, count, story)
  }

  /*
  val isKnot: Boolean
    get() = type == ContentType.KNOT

  val isFunction: Boolean
    get() = type == ContentType.FUNCTION

  val isStitch: Boolean
    get() = type == ContentType.STITCH

  val isChoice: Boolean
    get() = type == ContentType.CHOICE_ONCE || type == ContentType.CHOICE_REPEATABLE

  val isFallbackChoice: Boolean
    get() = isChoice && text.isEmpty()

  val isGather: Boolean
    get() = type == ContentType.GATHER

  val isDivert: Boolean
    get() = text.contains(Symbol.DIVERT) && !isVariable && !isChoice

  val isConditional: Boolean
    get() = type == ContentType.CONDITIONAL || isSequence

  private val isSequence: Boolean
    @SuppressWarnings("OverlyComplexBooleanExpression")
    get() = type == ContentType.SEQUENCE_CYCLE ||
        type == ContentType.SEQUENCE_ONCE ||
        type == ContentType.SEQUENCE_SHUFFLE ||
        type == ContentType.SEQUENCE_STOP

  internal val isVariable: Boolean
    get() = type == ContentType.VARIABLE_DECLARATION || type == ContentType.VARIABLE_EXPRESSION || type == ContentType.VARIABLE_RETURN

  internal val isVariableReturn: Boolean
    get() = type == ContentType.VARIABLE_RETURN

  val isContainer: Boolean
    get() = isKnot || isFunction || isStitch || isChoice || isGather
  */

}
