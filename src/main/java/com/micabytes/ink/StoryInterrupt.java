package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

public interface StoryInterrupt {

  @NonNls String getId();

  @NonNls String getInterrupt();

  @NonNls String getInterruptCondition();

  @NonNls String getInterruptFile();

  boolean isChoice();

  boolean isDivert();

  boolean isActive();

  void done();

}
