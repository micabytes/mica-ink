package com.micabytes.ink

internal class Divert(lineNumber: Int,
                      text: String,
                      parent: Container) : Content(Content.getId(parent), text, parent, lineNumber) {

  @Throws(InkRunTimeException::class)
  fun resolveDivert(story: Story): Container {
    var d = text.trim({ it <= ' ' })
    if (d.contains(StoryText.BRACE_LEFT))
      d = d.substring(0, d.indexOf(StoryText.BRACE_LEFT))
    //d = resolveInterrupt(d)
    val container = story.getDivert(d)
    container.count++
    return container
  }

}