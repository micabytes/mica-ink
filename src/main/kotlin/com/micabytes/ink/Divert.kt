package com.micabytes.ink

internal class Divert(lineNumber: Int,
                      text: String,
                      parent: Container?) : Content(lineNumber, text, parent) {

  @Throws(InkRunTimeException::class)
  fun resolveDivert(story: Story): Container {
    var d = text.trim({ it <= ' ' })
    if (d.contains(StoryText.BRACE_LEFT))
      d = d.substring(0, d.indexOf(StoryText.BRACE_LEFT))
    //d = resolveInterrupt(d)
    return story.getDivert(d)
  }

}