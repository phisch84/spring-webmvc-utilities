package com.schoste.ddd.application.v2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Example annotation for a method
 * 
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, })
public @interface ExampleMethodAnnotation 
{
	/**
	 * Gets or sets an example property of the annotation
	 * 
	 * @return the value of the property
	 */
	String exampleProperty();
}
