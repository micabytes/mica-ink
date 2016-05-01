package com.micabytes.ink;

public interface StoryInterrupt {

  public String getId();

  public String getInterrupt();

  public String getInterruptCondition();

  public String getInterruptFile();

  public boolean isChoice();

  public boolean isDivert();

  public boolean isActive();

  void done();

}
