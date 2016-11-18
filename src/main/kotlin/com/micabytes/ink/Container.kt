package com.micabytes.ink

import java.util.ArrayList

open class Container(l: Int, str: String, parent: Container?) : Content(l) {
    internal var level: Int = 0
    internal var content: MutableList<Content> = ArrayList()
    var background: String? = null

    open fun add(item: Content) {
        content.add(item)
    }

    internal fun getContainer(lvl: Int): Container {
        var c = this
        while (c.level > lvl && c.parent != null) {
            c = c.parent
        }
        return c
    }

    override fun generateId(p: Container): String {
        if (id != null) return id
        id = if (parent != null)
            parent!!.id + InkParser.DOT + Integer.toString(parent!!.getContentIndex(this))
        else
            super.generateId(p)
        return id
    }

    open val contentSize: Int
        get() = content.size

    open fun getContent(i: Int): Content {
        return content[i]
    }

    open fun getContentIndex(c: Content): Int {
        return content.indexOf(c)
    }

    @Throws(InkRunTimeException::class)
    open fun initialize(story: Story, c: Content) {
        increment()
    }

}
