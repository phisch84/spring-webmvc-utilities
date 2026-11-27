package com.schoste.ddd.application.v2.examples;

import org.springframework.beans.factory.annotation.Autowired;

import com.schoste.ddd.application.v2.dtos.standard.DataTransferObjectConverterImpl;
import com.schoste.ddd.application.v2.exceptions.ApplicationException;
import com.schoste.ddd.domain.v3.services.GenericRepository;

/**
 * Converter of the example in the package documentation of {@code com.schoste.ddd.application.v2.dtos.standard}
 */
public class CustomerDTOConverterImpl extends DataTransferObjectConverterImpl<Long, Customer, CustomerDTO> implements CustomerDTOConverter
{
    @Autowired
    protected CustomerRepository repository;

    @Override
    protected GenericRepository<Long, Customer, ?> getRepository() { return this.repository; }

    @Override
    public CustomerDTO createDataTransferObject() { return new CustomerDTO(); }

    @Override
    public CustomerDTO toDataTransferObject(Object... param) throws ApplicationException
    {
        if (param == null) return null;

        CustomerDTO dto = this.createDataTransferObject();

        if ((param.length > 0) && (param[0] instanceof String)) dto.setName((String) param[0]);

        return dto;
    }

    @Override
    public Object fromDataTransferObject(CustomerDTO dto) throws ApplicationException
    {
        return (dto == null) ? null : new Object[] { dto.getName() };
    }

    @Override
    protected void afterAutoConversion(Customer sourceEntity, CustomerDTO destinationDataTransferObject) { }

    @Override
    protected void afterAutoConversion(CustomerDTO sourceDataTransferObject, Customer destinationEntity) { }
}
