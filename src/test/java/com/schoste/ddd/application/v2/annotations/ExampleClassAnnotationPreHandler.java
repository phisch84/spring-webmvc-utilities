package com.schoste.ddd.application.v2.annotations;

/**
 * Example implementation of a handler for the {@link ExampleClassAnnotation} annotation.
 * It doesn't implement the annotation: its type argument tells the interceptor which annotation it handles.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ExampleClassAnnotationPreHandler extends RecordingPreHandler<ExampleClassAnnotation>
{
}
