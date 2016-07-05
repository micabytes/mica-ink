package com.micabytes.ink

import java.io.InputStream

import org.apache.commons.io.IOUtils

class StoryContainer extends StoryWrapper {

  override def getStream(fileId: String): InputStream = IOUtils.toInputStream(fileId, "UTF-8")

  override def getStoryObject(objId: String): AnyRef = {
    new Object()
  }

  override def getInterrupt(s: String): StoryInterrupt = {
    new StoryInterrupt {
      override def getId: String = "null"

      override def isDivert: Boolean = false

      override def done(): Unit = {
        // NOOP
      }

      override def isActive: Boolean = false

      override def isChoice: Boolean = false

      override def getInterrupt: String = ""

      override def getInterruptCondition: String = ""

      override def getInterruptFile: String = ""
    }
  }

  override def logError(m: String): Unit = {
    // NOOP
  }

  override def logException(e: Exception): Unit = {
    // NOOP
  }

  override def logDebug(m: String): Unit = {
    // NOOP
  }

}