package com.example.helloworld.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonJPADAO;
import com.google.inject.Provider;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PeopleResource.class);

	@com.google.inject.Inject
    private Provider<PersonJPADAO> peopleDAOProvider;

	
	public PeopleResource() {
    }

    @POST
//    @UnitOfWork
    public Person createPerson(Person person) {
    	logger.info("Using Injected peopleDAOProvider");
        return peopleDAOProvider.get().create(person);
    }

    @GET
//    @UnitOfWork
    public List<Person> listPeople() {
    	logger.info("Using Injected peopleDAOProvider");
        return peopleDAOProvider.get().findAll();
    }

}
