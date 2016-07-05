package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.io.InputStream;

/** StoryWrapper is the interface to the class used to convert file IDs/file names to and
 * InputStream.
 */
public interface StoryWrapper {
  InputStream getStream(String fileId);

  Object getStoryObject(String objId);

  StoryInterrupt getInterrupt(String s);

  void logDebug(String m);

  void logError(@NonNls String m);

  void logException(Exception e);
}
