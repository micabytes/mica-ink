package com.micabytes.ink


@SuppressWarnings("UtilityClass")
class StoryJson private constructor() {

    init {
        throw AssertionError("StoryJson should never be initialized.")
    }

    companion object {
         val FILE = "file"
         val FILES = "files"
        val CONTENT = "content"
        val ID = "id"
        val COUNT = "count"
        val VARIABLES = "variables"
        val CONTAINER = "container"
        val COUNTER = "counter"
        val TEXT = "text"
        val CHOICES = "choices"
        val CHOICES_DETAIL = "choice"
        val IMAGE = "image"
        val RUNNING = "running"
    }
}
