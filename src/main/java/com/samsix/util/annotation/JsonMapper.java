package com.samsix.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
public @interface JsonMapper
{
    /**
     * Forces using JAXB or JACKSON style annotations.
     */
    JsonMapperType value();
}
