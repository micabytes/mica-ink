package com.micabytes.ink

import java.io.InputStream

class TestWrapper : StoryWrapper {

  override fun getStoryObject(objId: String): Any {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getInterrupt(s: String): StoryInterrupt {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun logDebug(m: String) {
    // NOOP
  }

  override fun logError(m: String) {
    // NOOP
  }

  override fun logException(e: Exception) {
    // NOOP
  }

  override fun getStream(fileId: String): InputStream {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}