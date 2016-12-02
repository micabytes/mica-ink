package com.micabytes.ink

internal class Divert(lineNumber: Int,
                      text: String,
                      parent: Container?) : Container(lineNumber, text, parent) {

  @Throws(InkRunTimeException::class)
  fun resolveDivert(story: Story): Container {
    var d = text.trim({ it <= ' ' })
    if (d.contains(StoryText.BRACE_LEFT))
      d = d.substring(0, d.indexOf(StoryText.BRACE_LEFT))
    //d = resolveInterrupt(d)
    val divertTo: Content? = story.content.get(d)
    if (divertTo == null)
      throw InkRunTimeException("Attempt to divert to non-defined node " + d + " in line " + lineNumber)

    return (divertTo as Container)
  }

}