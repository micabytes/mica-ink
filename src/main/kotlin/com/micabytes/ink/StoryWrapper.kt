package com.micabytes.ink

import java.io.InputStream

/// StoryWrapper is the interface to the class used to convert file IDs/file names to and InputStream.
interface StoryWrapper {
  fun getStream(fileId: String): InputStream
  fun getStoryObject(objId: String): Any
  fun getInterrupt(s: String): StoryInterrupt
  fun resolveTag(t: String)
  fun logDebug(m: String)
  fun logError(m: String)
  fun logException(e: Exception)
}
