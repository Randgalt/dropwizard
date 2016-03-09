package com.example.helloworld.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.google.inject.Provider;

import io.dropwizard.hibernate.UnitOfWork;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PeopleResource.class);

	@com.google.inject.Inject
    private Provider<PersonDAO> peopleDAOProvider;

    private PersonDAO peopleDAO;
	
	public PeopleResource() {
    }
	
    public PeopleResource(Provider<PersonDAO> peopleDAO) {
        this.peopleDAOProvider = peopleDAO;
    }
    
    public PeopleResource(PersonDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @POST
    @UnitOfWork
    public Person createPerson(Person person) {
    	if (peopleDAO!=null) {
    		logger.info("Using Non-Injected peopleDAO");
    		return peopleDAO.create(person);
    	}
    	logger.info("Using Injected peopleDAOProvider");
        return peopleDAOProvider.get().create(person);
    }

    @GET
    @UnitOfWork
    public List<Person> listPeople() {
    	if (peopleDAO!=null) {
    		logger.info("Using Non-Injected peopleDAO");
    		return peopleDAO.findAll();
    	}
    	logger.info("Using Injected peopleDAOProvider");
        return peopleDAOProvider.get().findAll();
    }

}
