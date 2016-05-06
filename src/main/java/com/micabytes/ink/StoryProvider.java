package com.micabytes.ink;

import java.io.InputStream;

/** Story Provider is the interface to the class used to convert file IDs/file names to and
 * InputStream.
 */
public interface StoryProvider {
  public InputStream getStream(String fileId);

  public Object getStoryObject(String objId);

  StoryInterrupt getInterrupt(String s);

  public void logDebug(String m);

  public void logError(String m);

  public void logException(Exception e);
}
