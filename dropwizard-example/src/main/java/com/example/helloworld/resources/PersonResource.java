package com.example.helloworld.resources;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonJPADAO;
import com.google.common.base.Optional;
import com.google.inject.Provider;

import io.dropwizard.jersey.params.LongParam;

@Path("/people/{personId}")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersonResource.class);

	//to test soabase injection
	@javax.inject.Inject
    private Provider<PersonJPADAO> peopleDAOProvider;
	

	public PersonResource() {
		System.out.println("PersonResource instantiated");
		logger.info("PersonResource instantiated");
    }
	

    @GET
    public Person getPerson(@PathParam("personId") LongParam personId) {
        return findSafely(personId.get());
    }

    private Person findSafely(long personId) {
    	final Optional<Person> person;
		
    	logger.info("Using Injected peopleDAOProvider");
    	person = peopleDAOProvider.get().findById(personId);
        
        if (!person.isPresent()) {
            throw new NotFoundException("No such user.");
        }
        return person.get();
    }
}
