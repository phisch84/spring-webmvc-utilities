package com.schoste.ddd.application.v2.dtos.standard;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import com.schoste.ddd.application.v2.dtos.MockedDataTransferObjectA;
import com.schoste.ddd.application.v2.dtos.mocked.MockedDataTransferObjectAConverterImpl;
import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.application.v2.exceptions.EntityNotFoundException;
import com.schoste.ddd.application.v2.exceptions.NoRepositoryException;
import com.schoste.ddd.application.v2.testfixtures.ExampleEntity;
import com.schoste.ddd.domain.v3.exceptions.DomainException;
import com.schoste.ddd.domain.v3.services.GenericRepository;

/**
 * Tests which exceptions the methods of {@link DataTransferObjectConverterImpl} pass on unchanged
 * ({@link DomainException} and {@link ApplicationException} with its subclasses) and which they wrap
 * (every other exception, in an {@link ApplicationException} with the exception as cause).
 * The collection methods are tested with a converter whose single-object methods fail.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class DataTransferObjectConverterImplExceptionTest
{
	/**
	 * Converter whose single-object methods throw a given exception, as far as their signatures allow it
	 */
	protected static class FailingConverter extends MockedDataTransferObjectAConverterImpl
	{
		protected final Exception failure;

		public FailingConverter(Exception failure)
		{
			this.failure = failure;
		}

		protected void fail(boolean domainExceptionAllowed) throws DomainException, ApplicationException
		{
			if (this.failure instanceof RuntimeException) throw (RuntimeException) this.failure;
			if (this.failure instanceof ApplicationException) throw (ApplicationException) this.failure;
			if (domainExceptionAllowed && (this.failure instanceof DomainException)) throw (DomainException) this.failure;

			throw new IllegalStateException("the failure can't be thrown by this method: " + this.failure);
		}

		@Override
		public MockedDataTransferObjectA toDataTransferObject(ExampleEntity entity) throws DomainException, ApplicationException
		{
			this.fail(true);

			return null;
		}

		@Override
		public MockedDataTransferObjectA toDataTransferObject(Object... param) throws ApplicationException
		{
			try
			{
				this.fail(false);
			}
			catch (DomainException e)
			{
				throw new IllegalStateException(e);
			}

			return null;
		}

		@Override
		public Object fromDataTransferObject(MockedDataTransferObjectA dto) throws ApplicationException
		{
			return this.toDataTransferObject(new Object[0]);
		}

		@Override
		public ExampleEntity toEntity(MockedDataTransferObjectA dto) throws DomainException, ApplicationException
		{
			this.fail(true);

			return null;
		}
	}

	protected final DomainException domainException = new DomainException("domain");
	protected final ApplicationException applicationException = new EntityNotFoundException(4711L);
	protected final RuntimeException runtimeException = new IllegalArgumentException("other");

	protected final Collection<ExampleEntity> entities = Collections.singletonList(new ExampleEntity());
	protected final Collection<Object[]> params = Collections.singletonList(new Object[0]);
	protected final Collection<MockedDataTransferObjectA> dtos = Arrays.asList(new MockedDataTransferObjectA());

	protected void assertPassedOn(Exception expected, ThrowingRunnable call)
	{
		Throwable actual = Assert.assertThrows(Throwable.class, call);

		Assert.assertSame(expected, actual);
	}

	protected void assertWrapped(Exception expected, ThrowingRunnable call)
	{
		ApplicationException actual = Assert.assertThrows(ApplicationException.class, call);

		Assert.assertEquals(ApplicationException.class, actual.getClass());
		Assert.assertSame(expected, actual.getCause());
	}

	@Test
	public void testToDataTransferObjectsFromEntitiesPassesOnDomainException()
	{
		FailingConverter converter = new FailingConverter(this.domainException);

		this.assertPassedOn(this.domainException, () -> converter.toDataTransferObjectsFromEntities(this.entities));
	}

	@Test
	public void testToDataTransferObjectsFromEntitiesPassesOnApplicationException()
	{
		FailingConverter converter = new FailingConverter(this.applicationException);

		this.assertPassedOn(this.applicationException, () -> converter.toDataTransferObjectsFromEntities(this.entities));
	}

	@Test
	public void testToDataTransferObjectsFromEntitiesWrapsOtherExceptions()
	{
		FailingConverter converter = new FailingConverter(this.runtimeException);

		this.assertWrapped(this.runtimeException, () -> converter.toDataTransferObjectsFromEntities(this.entities));
	}

	@Test
	public void testToDataTransferObjectsPassesOnApplicationException()
	{
		FailingConverter converter = new FailingConverter(this.applicationException);

		this.assertPassedOn(this.applicationException, () -> converter.toDataTransferObjects(this.params));
	}

	@Test
	public void testToDataTransferObjectsWrapsOtherExceptions()
	{
		FailingConverter converter = new FailingConverter(this.runtimeException);

		this.assertWrapped(this.runtimeException, () -> converter.toDataTransferObjects(this.params));
	}

	@Test
	public void testFromDataTransferObjectsPassesOnApplicationException()
	{
		FailingConverter converter = new FailingConverter(this.applicationException);

		this.assertPassedOn(this.applicationException, () -> converter.fromDataTransferObjects(this.dtos));
	}

	@Test
	public void testFromDataTransferObjectsWrapsOtherExceptions()
	{
		FailingConverter converter = new FailingConverter(this.runtimeException);

		this.assertWrapped(this.runtimeException, () -> converter.fromDataTransferObjects(this.dtos));
	}

	@Test
	public void testToEntitiesPassesOnDomainException()
	{
		FailingConverter converter = new FailingConverter(this.domainException);

		this.assertPassedOn(this.domainException, () -> converter.toEntities(this.dtos));
	}

	@Test
	public void testToEntitiesPassesOnApplicationException()
	{
		FailingConverter converter = new FailingConverter(this.applicationException);

		this.assertPassedOn(this.applicationException, () -> converter.toEntities(this.dtos));
	}

	@Test
	public void testToEntitiesWrapsOtherExceptions()
	{
		FailingConverter converter = new FailingConverter(this.runtimeException);

		this.assertWrapped(this.runtimeException, () -> converter.toEntities(this.dtos));
	}

	/**
	 * Creates a converter whose repository regards every id as actual and throws a given exception when an entity is requested
	 *
	 * @param failure the exception the repository throws
	 * @return the converter
	 */
	@SuppressWarnings("unchecked")
	protected MockedDataTransferObjectAConverterImpl createConverterWithFailingRepository(Exception failure)
	{
		GenericRepository<Long, ExampleEntity, ?> failingRepository = (GenericRepository<Long, ExampleEntity, ?>) Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class<?>[] { GenericRepository.class },
				(proxy, method, args) ->
				{
					if (method.getName().equals("isVirtualId")) return false;

					throw failure;
				});

		return new MockedDataTransferObjectAConverterImpl()
		{
			@Override
			public GenericRepository<Long, ExampleEntity, ?> getRepository()
			{
				return failingRepository;
			}
		};
	}

	protected MockedDataTransferObjectA createDTO(Long id)
	{
		MockedDataTransferObjectA dto = new MockedDataTransferObjectA();

		dto.setId(id);

		return dto;
	}

	@Test
	public void testCreateEntityPassesOnDomainException()
	{
		MockedDataTransferObjectAConverterImpl converter = this.createConverterWithFailingRepository(this.domainException);

		this.assertPassedOn(this.domainException, () -> converter.createEntity(this.createDTO(1L)));
	}

	@Test
	public void testToEntityPassesOnDomainExceptionOfRepository()
	{
		MockedDataTransferObjectAConverterImpl converter = this.createConverterWithFailingRepository(this.domainException);

		this.assertPassedOn(this.domainException, () -> converter.toEntity(this.createDTO(1L)));
	}

	@Test
	public void testCreateEntityWrapsOtherExceptions()
	{
		MockedDataTransferObjectAConverterImpl converter = this.createConverterWithFailingRepository(this.runtimeException);

		this.assertWrapped(this.runtimeException, () -> converter.createEntity(this.createDTO(1L)));
	}

	/**
	 * Asserts that the messages of the exceptions are found in the library's resource bundle
	 */
	@Test
	public void testExceptionMessages()
	{
		Assert.assertEquals("No repository has been defined", new NoRepositoryException().getMessage());
		Assert.assertEquals("No entity with id 4711 has been found", new EntityNotFoundException(4711L).getMessage());
	}
}
