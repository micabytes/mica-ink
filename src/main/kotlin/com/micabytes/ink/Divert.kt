package com.micabytes.ink

import com.micabytes.ink.util.InkRunTimeException

internal class Divert(lineNumber: Int,
                      text: String,
                      parent: Container) : Content(Content.getId(parent), text, parent, lineNumber) {

  @Throws(InkRunTimeException::class)
  fun resolveDivert(story: Story): Container {
    var d = text.trim({ it <= ' ' })
    if (d.contains(Symbol.BRACE_LEFT))
      d = d.substring(0, d.indexOf(Symbol.BRACE_LEFT))
    d = story.resolveInterrupt(d)
    return story.getDivert(d)
    //container.count++ // TODO: Not sure I understand this
  }

}