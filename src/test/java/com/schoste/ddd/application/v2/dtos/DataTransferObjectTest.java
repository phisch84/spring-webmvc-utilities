package com.schoste.ddd.application.v2.dtos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.schoste.ddd.application.v2.testfixtures.ExampleDTO;
import com.schoste.ddd.application.v2.testfixtures.ExampleEntity;

/**
 * Tests {@link DataTransferObject}
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public class DataTransferObjectTest
{
	/**
	 * Decision 6: the entity of a DTO is never serialized; its id, flags and properties are
	 *
	 * @throws Exception re-throws every exception
	 */
	@Test
	public void testSerializationLeavesOutEntity() throws Exception
	{
		ExampleDTO dto = new ExampleDTO();

		dto.setId(4711L);
		dto.setEntity(new ExampleEntity());
		dto.setDeleted(true);
		dto.setAddedOrModified(false);
		dto.setExampleProperty1("serialized");

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		try (ObjectOutputStream out = new ObjectOutputStream(bytes))
		{
			out.writeObject(dto);
		}

		ExampleDTO copy;

		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())))
		{
			copy = (ExampleDTO) in.readObject();
		}

		Assert.assertNull(copy.getEntity());
		Assert.assertEquals(Long.valueOf(4711L), copy.getId());
		Assert.assertEquals(Boolean.TRUE, copy.isDeleted());
		Assert.assertEquals(Boolean.FALSE, copy.isAddedOrModified());
		Assert.assertEquals("serialized", copy.getExampleProperty1());
	}

	/**
	 * Decision 6: the flags are unset (null) until the server or client sets them
	 */
	@Test
	public void testFlagsAreUnsetByDefault()
	{
		ExampleDTO dto = new ExampleDTO();

		Assert.assertNull(dto.isDeleted());
		Assert.assertNull(dto.isAddedOrModified());
		Assert.assertNull(dto.getId());
		Assert.assertNull(dto.getEntity());
	}
}
