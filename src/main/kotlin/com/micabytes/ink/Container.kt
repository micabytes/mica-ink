package com.micabytes.ink

import java.util.ArrayList

open class Container(id: String,
                     text: String,
                     parent: Container?,
                     lineNumber: Int) : Content(id, text, parent, lineNumber) {
  internal var index: Int = 0
  internal var children: MutableList<Content> = ArrayList()

  fun add(item: Content) {
    children.add(item)
  }

  fun get(i: Int): Content {
    return children[i]
  }

  fun indexOf(c: Content): Int {
    return children.indexOf(c)
  }

  val size: Int
    get() = children.size

}
