package com.micabytes.ink

open class Content(val lineNumber: Int, val content: String, val parent: Container) {
  val id: String
  // internal set
  // internal var lineNumber: Int = 0
  // internal set
  internal var type = ContentType.TEXT
  internal var text = ""
  var count: Int = 0

  init {
    id = generateId(parent)
  }

  /*
  internal constructor() {
      // NOOP
  }

  constructor(l: Int, str: String, current: Container) {
      lineNumber = l
      text = str
      current.add(this)
  }
  */

  open fun generateId(p: Container): String {
    val i = p.getContentIndex(this)
    return p.id + InkParser.DOT + Integer.toString(i)
  }

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

  fun increment() {
    count++
  }

  internal val isVariable: Boolean
    get() = type == ContentType.VARIABLE_DECLARATION || type == ContentType.VARIABLE_EXPRESSION || type == ContentType.VARIABLE_RETURN

  internal val isVariableReturn: Boolean
    get() = type == ContentType.VARIABLE_RETURN

  val isContainer: Boolean
    get() = isKnot || isFunction || isStitch || isChoice || isGather

}
