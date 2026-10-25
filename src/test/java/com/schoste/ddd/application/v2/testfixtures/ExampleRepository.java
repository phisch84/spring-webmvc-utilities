package com.schoste.ddd.application.v2.testfixtures;

import com.schoste.ddd.domain.v3.services.GenericRepository;

/**
 * Repository of {@link ExampleEntity} used for testing
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
public interface ExampleRepository extends GenericRepository<Long, ExampleEntity, ExampleDO>
{

}
