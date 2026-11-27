package com.schoste.ddd.application.v2.interceptor;

import java.lang.annotation.Annotation;

import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * Determines the annotation of a handler from the type argument of its base class
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
final class AnnotationTypes
{
	private AnnotationTypes()
	{
	}

	/**
	 * Gets the type argument of a handler's base class, as bound by the class of the handler (or one of its superclasses)
	 *
	 * @param handler the handler
	 * @param baseClass the generic base class of the handler, whose only type argument is the annotation
	 * @return the annotation type
	 * @throws IllegalStateException thrown if the type argument isn't bound to a concrete annotation
	 */
	@SuppressWarnings("unchecked")
	static Class<? extends Annotation> of(Object handler, Class<?> baseClass) throws IllegalStateException
	{
		Class<?> annotationType = ResolvableType.forClass(handler.getClass()).as(baseClass).resolveGeneric(0);

		// an open type variable resolves to its bound (Annotation itself), which isn't an annotation type
		if ((annotationType == null) || !annotationType.isAnnotation())
		{
			throw new IllegalStateException(String.format("The annotation of the handler %s can't be determined: its class must bind the type argument of %s to an annotation, e.g. \"extends %s<MyAnnotation>\"",
					ClassUtils.getUserClass(handler).getName(), baseClass.getSimpleName(), baseClass.getSimpleName()));
		}

		return (Class<? extends Annotation>) annotationType;
	}
}
