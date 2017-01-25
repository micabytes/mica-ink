package com.micabytes.ink

open class Content(val id: String,
                   val text: String,
                   val parent: Container?,
                   val lineNumber: Int) {

  internal var count: Int = 0

  open fun  getText(story: Story): String {
    return StoryText.getText(text, count, story)
  }

  companion object {

    fun getId(parent: Container): String {
      return parent.id + InkParser.DOT + parent.size
    }

  }

}
