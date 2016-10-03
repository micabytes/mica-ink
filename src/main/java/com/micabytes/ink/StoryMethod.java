package com.micabytes.ink;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Used to mark methods used in ink stories
// Convenience annotation
/*@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD) //can use in method only.
public @interface StoryMethod {
  // NOOP
}*/

@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface StoryMethod {
  // NOOP
}


/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
 */
//package proguard.annotation;
//    import java.lang.annotation.*;
/**
 * This annotation specifies not to optimize or obfuscate the annotated class or
 * class member as an entry point.
 */
