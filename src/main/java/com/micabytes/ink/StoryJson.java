package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

@SuppressWarnings("UtilityClass")
public final class StoryJson
{
  @NonNls public static final String FILE = "file";
  @NonNls public static final String FILES = "files";
  public static final String CONTENT = "content";
  public static final String ID = "id";
  public static final String COUNT = "count";
  public static final String VARIABLES = "variables";
  public static final String CONTAINER = "container";
  public static final String COUNTER = "counter";
  public static final String TEXT = "text";
  public static final String CHOICES = "choices";
  public static final String CHOICES_DETAIL = "choice";
  public static final String IMAGE = "image";
  public static final String RUNNING = "running";

  private StoryJson() {
    throw new AssertionError("StoryJson should never be initialized.");
  }
}
