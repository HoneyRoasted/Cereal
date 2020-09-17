package io.github.honeyroasted.cereal.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Cerealizable {

    boolean reflective() default false;

    boolean useBaseType() default false;

}
