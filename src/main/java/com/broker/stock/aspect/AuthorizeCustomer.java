package com.broker.stock.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If you want to activate this annotation, please make sure that Method params/object should have the
 * {@link CustomerId}
 * annotation. Otherwise, method will not be authorized properly.
 */
@Target(ElementType.METHOD) // The annotation can only be applied to methods
@Retention(RetentionPolicy.RUNTIME) // The annotation will be available at runtime
public @interface AuthorizeCustomer {
}
