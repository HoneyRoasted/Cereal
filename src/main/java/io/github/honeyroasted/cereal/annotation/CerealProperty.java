package io.github.honeyroasted.cereal.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CerealProperty {
    String FIELD_NAME = "<FIELD_NAME>";

    String value() default FIELD_NAME;

    boolean reflective() default false;

    boolean useBaseType() default false;

}
