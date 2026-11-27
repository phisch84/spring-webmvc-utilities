package com.schoste.ddd.application.v2.examples;

import com.schoste.ddd.domain.v3.services.GenericRepository;

/**
 * Repository of {@link Customer}
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public interface CustomerRepository extends GenericRepository<Long, Customer, CustomerDO>
{
}
