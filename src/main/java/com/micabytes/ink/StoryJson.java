package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

@SuppressWarnings("UtilityClass")
public final class StoryJson
{
  @NonNls public static final String FILE = "fl";
  public static final String CONTENT = "c$";
  public static final String ID = "id";
  public static final String COUNT = "n#";
  public static final String VARIABLES = "v$";
  public static final String CONTAINER = "h$";
  public static final String COUNTER = "h#";
  public static final String CHOICES = "hc";
  public static final String IMAGE = "hp";
  public static final String VARIABLES_GLOBAL = "hv";
  public static final String RUNNING = "hr";

  private StoryJson() {
    throw new AssertionError("StoryJson should never be initialized.");
  }
}
