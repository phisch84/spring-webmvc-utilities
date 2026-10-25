/**
 * Standard implementation of the data transfer object converter.
 * <p>
 * Example of a converter for an entity {@code Customer} with {@code Long} ids, its repository {@code CustomerRepository}
 * and its DTO {@code CustomerDTO}, whose setters are annotated with {@link com.schoste.ddd.domain.v3.annotations.AutoSet}
 * (as are the setters of the entity):
 * <pre>
 * public interface CustomerDTOConverter extends DataTransferObjectConverter&lt;Long, Customer, CustomerDTO&gt;
 * {
 * }
 *
 * public class CustomerDTOConverterImpl extends DataTransferObjectConverterImpl&lt;Long, Customer, CustomerDTO&gt; implements CustomerDTOConverter
 * {
 *     &#64;Autowired
 *     protected CustomerRepository repository;
 *
 *     &#64;Override
 *     protected GenericRepository&lt;Long, Customer, ?&gt; getRepository() { return this.repository; }
 *
 *     &#64;Override
 *     public CustomerDTO createDataTransferObject() { return new CustomerDTO(); }
 *
 *     &#64;Override
 *     public CustomerDTO toDataTransferObject(Object... param) throws ApplicationException
 *     {
 *         if (param == null) return null;
 *
 *         CustomerDTO dto = this.createDataTransferObject();
 *
 *         if ((param.length &gt; 0) &amp;&amp; (param[0] instanceof String)) dto.setName((String) param[0]);
 *
 *         return dto;
 *     }
 *
 *     &#64;Override
 *     public Object fromDataTransferObject(CustomerDTO dto) throws ApplicationException
 *     {
 *         return (dto == null) ? null : new Object[] { dto.getName() };
 *     }
 *
 *     &#64;Override
 *     protected void afterAutoConversion(Customer sourceEntity, CustomerDTO destinationDataTransferObject) { }
 *
 *     &#64;Override
 *     protected void afterAutoConversion(CustomerDTO sourceDataTransferObject, Customer destinationEntity) { }
 * }
 * </pre>
 * The converter is registered as a bean of the Spring application context, e.g.
 * {@code <bean id="CustomerDTOConverter" class="CustomerDTOConverterImpl" scope="prototype" />}.
 * A typical use in a controller, which saves a changed or new customer sent by the client:
 * <pre>
 * try (UnitOfWork uow = this.applicationContext.getBean(UnitOfWork.class, this.customerRepository))
 * {
 *     Customer customer = this.customerDTOConverter.toEntity(customerDTO);
 *
 *     if (this.customerRepository.isVirtualId(customer.getId())) this.customerRepository.add(customer);
 *     else this.customerRepository.setModified(customer);
 *
 *     uow.commit();
 * }
 * </pre>
 */
package com.schoste.ddd.application.v2.dtos.standard;
