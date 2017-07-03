package com.micabytes.ink.util

class InkRunTimeException : Exception {

  constructor(message: String) : super(message)

  constructor(throwable: Throwable) : super(throwable)

  constructor(message: String, throwable: Throwable) : super(message, throwable)

}
