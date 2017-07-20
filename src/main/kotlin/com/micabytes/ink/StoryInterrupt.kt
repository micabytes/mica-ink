package com.micabytes.ink

abstract class StoryInterrupt(
    val id: String,
    val text: String,
    val condition: String,
    val file: String) {
  val isChoice = text.startsWith(Symbol.CHOICE_DOT) || text.startsWith(Symbol.CHOICE_PLUS)
  val isDivert = text.startsWith(Symbol.DIVERT)
  var isActive: Boolean = true
}
