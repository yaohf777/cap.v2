package com.sap.grc.iag.accesscertification.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)

public @interface SapSemantics {
    String semantics() default "aggregate";
}