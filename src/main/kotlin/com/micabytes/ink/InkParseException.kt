package com.micabytes.ink

@SuppressWarnings("unused")
class InkParseException : Exception {

    constructor(message: String) : super(message) {
    }

    constructor(throwable: Throwable) : super(throwable) {
    }

    constructor(message: String, throwable: Throwable) : super(message, throwable) {
    }

}
