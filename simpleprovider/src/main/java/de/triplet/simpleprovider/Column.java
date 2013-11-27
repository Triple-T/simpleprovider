package de.triplet.simpleprovider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    FieldType value();

    boolean primaryKey() default false;

    boolean notNull() default false;

    boolean unique() default false;

    int since() default 1;

    enum FieldType {NULL, INTEGER, FLOAT, TEXT, BLOB}

}
