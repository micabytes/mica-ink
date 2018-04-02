package com.micabytes.ink


@SuppressWarnings("UtilityClass")
class StoryJson private constructor() {

  init {
    throw AssertionError("StoryJson should never be initialized.")
  }

  companion object {
    const val FILE = "file"
    const val FILES = "files"
    const val CONTENT = "children"
    const val ID = "id"
    const val COUNT = "count"
    const val VARIABLES = "values"
    const val CONTAINER = "container"
    const val COUNTER = "counter"
    const val TEXT = "text"
    const val CHOICES = "choices"
    const val CHOICES_DETAIL = "choice"
    const val IMAGE = "image"
    const val RUNNING = "running"
    const val INDEX = "index"
  }
}
