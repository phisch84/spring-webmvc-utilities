package com.schoste.ddd.application.v2.examples;

import com.schoste.ddd.application.v2.dtos.DataTransferObjectConverter;

/**
 * Converter interface of the example in the package documentation of {@code com.schoste.ddd.application.v2.dtos.standard}
 */
public interface CustomerDTOConverter extends DataTransferObjectConverter<Long, Customer, CustomerDTO>
{
}
