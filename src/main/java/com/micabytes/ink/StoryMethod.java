package com.micabytes.ink;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Used to mark methods used in ink stories
// Convenience annotation
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD) //can use in method only.
public @interface StoryMethod {
  // NOOP
}