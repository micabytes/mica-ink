package com.micabytes.ink

import com.micabytes.ink.util.InkParseException
import com.micabytes.ink.util.InkRunTimeException
import java.util.*

internal class Conditional @Throws(InkParseException::class)
constructor(header: String,
            parent: Container,
            lineNumber: Int) : Container(getId(parent), "", parent, lineNumber) {

  internal enum class SequenceType {
    SEQUENCE_NONE,
    SEQUENCE_CYCLE,
    SEQUENCE_ONCE,
    SEQUENCE_SHUFFLE,
    SEQUENCE_STOP
  }

  var seqType: SequenceType = SequenceType.SEQUENCE_NONE

  init {
    val str = header.substring(1).trim({ it <= ' ' })
    if (!str.isEmpty()) {
      if (!str.endsWith(":"))
        throw InkParseException("Error in conditional block; initial condition not ended by \':\'. Line number: $lineNumber")
      val condition = str.substring(0, str.length - 1).trim({ it <= ' ' })
      verifySequenceCondition(condition)
      if (seqType == SequenceType.SEQUENCE_NONE) {
        children.add(ConditionalOption(header, this, lineNumber))
      }
    }
  }

  fun  resolveConditional(story: Story): Container {
    index = size
    when (seqType) {
      SequenceType.SEQUENCE_NONE -> {
        for (opt in children) {
          if ((opt as ConditionalOption).evaluate(story)) {
            return opt
          }
        }
      }
      SequenceType.SEQUENCE_CYCLE -> return children[count % children.size] as Container
      SequenceType.SEQUENCE_ONCE -> if (count < size) return children[count] as Container
      SequenceType.SEQUENCE_SHUFFLE -> return children[Random().nextInt(children.size)] as Container
      SequenceType.SEQUENCE_STOP -> return children[if (count >= children.size) children.size - 1 else count] as Container
      else -> story.logException(InkRunTimeException("Invalid conditional type."))
    }
    val empty = ConditionalOption("", this, 0)
    children.remove(empty)
    return empty
  }

  private fun verifySequenceCondition(str: String) {
    if (STOPPING.equals(str, ignoreCase = true))
      seqType = SequenceType.SEQUENCE_STOP
    if (SHUFFLE.equals(str, ignoreCase = true))
      seqType = SequenceType.SEQUENCE_SHUFFLE
    if (CYCLE.equals(str, ignoreCase = true))
      seqType = SequenceType.SEQUENCE_CYCLE
    if (ONCE.equals(str, ignoreCase = true))
      seqType = SequenceType.SEQUENCE_ONCE
  }

  companion object {
    private val STOPPING = "stopping"
    private val SHUFFLE = "shuffle"
    private val CYCLE = "cycle"
    private val ONCE = "once"

    fun isConditionalHeader(str: String): Boolean {
      return str.startsWith(Symbol.CBRACE_LEFT) && !str.contains(Symbol.CBRACE_RIGHT)
    }

  }

}
