package com.micabytes.ink

internal class Divert(lineNumber: Int,
                      content: String,
                      parent: Container?) : Container(lineNumber, content, parent) {
  fun resolveDivert(story: Story): Container {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}