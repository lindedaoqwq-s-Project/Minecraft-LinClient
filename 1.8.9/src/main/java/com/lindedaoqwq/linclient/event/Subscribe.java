package com.lindedaoqwq.linclient.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a method as an event listener for {@link EventBus}.
 *  The method must be public, take exactly one {@link Event} parameter and return void.
 *  Public is required: Java 8's MethodHandles.lookup() cannot unreflect non-public
 *  methods of classes in other packages. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
}
