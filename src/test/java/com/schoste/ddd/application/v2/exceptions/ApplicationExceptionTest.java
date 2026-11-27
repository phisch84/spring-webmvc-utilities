package com.schoste.ddd.application.v2.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the global {@link ApplicationException#handler} and the messages of the exceptions (README "Exceptions";
 * decision 12)
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class ApplicationExceptionTest
{
	@After
	public void tearDown()
	{
		ApplicationException.handler = null;
	}

	/**
	 * The handler is notified of every application exception when it is created, with every constructor and also for subclasses
	 */
	@Test
	public void testHandlerIsNotifiedOfEveryException()
	{
		List<ApplicationException> created = new ArrayList<>();

		ApplicationException.handler = created::add;

		Exception cause = new IllegalStateException("cause");
		ApplicationException withMessage = new ApplicationException("message");
		ApplicationException withCause = new ApplicationException(cause);
		ApplicationException withBoth = new ApplicationException("message", cause);
		NoRepositoryException noRepository = new NoRepositoryException();
		EntityNotFoundException entityNotFound = new EntityNotFoundException(4711L);

		Assert.assertEquals(5, created.size());
		Assert.assertSame(withMessage, created.get(0));
		Assert.assertSame(withCause, created.get(1));
		Assert.assertSame(withBoth, created.get(2));
		Assert.assertSame(noRepository, created.get(3));
		Assert.assertSame(entityNotFound, created.get(4));

		Assert.assertEquals("message", withBoth.getMessage());
		Assert.assertSame(cause, withBoth.getCause());
		Assert.assertSame(cause, withCause.getCause());
	}

	/**
	 * A failing handler never replaces the exception which is being created: its exception is printed to {@code System.err}
	 */
	@Test
	public void testFailingHandlerDoesNotPropagate()
	{
		PrintStream originalErr = System.err;
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		ApplicationException.handler = exception -> { throw new IllegalStateException("handler failed"); };

		try
		{
			System.setErr(new PrintStream(err, true));

			ApplicationException exception = new ApplicationException("created anyway");

			Assert.assertEquals("created anyway", exception.getMessage());
		}
		finally
		{
			System.setErr(originalErr);
		}

		Assert.assertTrue(new String(err.toByteArray(), StandardCharsets.UTF_8).contains("handler failed"));
	}

	/**
	 * Decision 12: the messages of the exceptions are found in the library's own resource bundle
	 */
	@Test
	public void testMessagesComeFromLibraryBundle()
	{
		Assert.assertEquals("No repository has been defined", new NoRepositoryException().getMessage());
		Assert.assertEquals("No entity with id 4711 has been found", new EntityNotFoundException(4711L).getMessage());
		Assert.assertEquals(Long.valueOf(4711L), new EntityNotFoundException(4711L).getId());
	}
}
