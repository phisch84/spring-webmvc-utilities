package com.schoste.ddd.application.v2.interceptor;

import org.junit.Assert;
import org.junit.Test;

import com.schoste.ddd.application.v2.annotations.ExampleMethodAnnotation;
import com.schoste.ddd.application.v2.annotations.RecordingPostHandler;
import com.schoste.ddd.application.v2.annotations.RecordingPreHandler;

/**
 * Tests the handler base classes {@link GenericAnnotationPreHandler} and {@link GenericAnnotationPostHandler} on their own;
 * how the interceptor uses them is tested by {@link AnnotationInterceptorTest}
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class GenericAnnotationHandlerTest
{
	/**
	 * Decision 8: handlers are ordered by their priority, lowest number first; equal priorities compare as equal (their order
	 * is undefined). The default priority is 0.
	 */
	@Test
	public void testPreHandlersCompareByPriority()
	{
		RecordingPreHandler<ExampleMethodAnnotation> low = new RecordingPreHandler<ExampleMethodAnnotation>() { };
		RecordingPreHandler<ExampleMethodAnnotation> high = new RecordingPreHandler<ExampleMethodAnnotation>() { };
		RecordingPreHandler<ExampleMethodAnnotation> alsoHigh = new RecordingPreHandler<ExampleMethodAnnotation>() { };

		Assert.assertEquals(0, low.getPriority());

		low.setPriority(-3);
		high.setPriority(7);
		alsoHigh.setPriority(7);

		Assert.assertEquals(7, high.getPriority());
		Assert.assertTrue(low.compareTo(high) < 0);
		Assert.assertTrue(high.compareTo(low) > 0);
		Assert.assertEquals(0, high.compareTo(alsoHigh));
	}

	/**
	 * Decision 8: as {@link #testPreHandlersCompareByPriority()}, for post handlers
	 */
	@Test
	public void testPostHandlersCompareByPriority()
	{
		RecordingPostHandler<ExampleMethodAnnotation> low = new RecordingPostHandler<ExampleMethodAnnotation>() { };
		RecordingPostHandler<ExampleMethodAnnotation> high = new RecordingPostHandler<ExampleMethodAnnotation>() { };
		RecordingPostHandler<ExampleMethodAnnotation> alsoHigh = new RecordingPostHandler<ExampleMethodAnnotation>() { };

		Assert.assertEquals(0, low.getPriority());

		low.setPriority(-3);
		high.setPriority(7);
		alsoHigh.setPriority(7);

		Assert.assertEquals(7, high.getPriority());
		Assert.assertTrue(low.compareTo(high) < 0);
		Assert.assertTrue(high.compareTo(low) > 0);
		Assert.assertEquals(0, high.compareTo(alsoHigh));
	}

	/**
	 * Decision 7: the annotation of a handler is the type argument its class binds, also through an intermediate generic
	 * base class and in an anonymous class
	 */
	@Test
	public void testAnnotationTypeIsTypeArgument()
	{
		Assert.assertEquals(ExampleMethodAnnotation.class, new RecordingPreHandler<ExampleMethodAnnotation>() { }.getAnnotationType());
		Assert.assertEquals(ExampleMethodAnnotation.class, new RecordingPostHandler<ExampleMethodAnnotation>() { }.getAnnotationType());
		Assert.assertThrows(IllegalStateException.class, () -> new OpenPreHandler<ExampleMethodAnnotation>().getAnnotationType());
	}
}
