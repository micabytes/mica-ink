package com.micabytes.ink

import java.util.ArrayList

open class Container(lineNumber: Int,
                     content: String,
                     parent: Container?) : Content(lineNumber, content, parent) {
  //open internal val level: Int = 0
  internal var children: MutableList<Content> = ArrayList()
  internal var background: String? = null

  fun add(item: Content) {
    children.add(item)
  }

  fun get(i: Int): Content {
    return children[i]
  }

  val size: Int
    get() = children.size

  fun indexOf(c: Content): Int {
    return children.indexOf(c)
  }

  /*
  internal fun getParent(lvl: Int): Container {
    var c: Container = this
    while (c.level > lvl && c.parent != null) {
      c = c.parent!!
    }
    return c
  }
  */

  @Throws(InkRunTimeException::class)
  open fun initialize(story: Story, c: Content) {
    count++
  }

}
