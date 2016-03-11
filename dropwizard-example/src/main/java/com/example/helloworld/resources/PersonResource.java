package com.example.helloworld.resources;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.views.PersonView;
import com.google.common.base.Optional;
import com.google.inject.Provider;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;

@Path("/people/{personId}")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersonResource.class);

	//to test soabase injection
	@javax.inject.Inject
    private Provider<PersonDAO> peopleDAOProvider;
	
	//to instantiate resource using "new"
	private PersonDAO peopleDAO;

	public PersonResource() {
    }
	
    public PersonResource(Provider<PersonDAO> peopleDAO) {
        this.peopleDAOProvider = peopleDAO;
    }
    
    public PersonResource(PersonDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @GET
    @UnitOfWork
    public Person getPerson(@PathParam("personId") LongParam personId) {
        return findSafely(personId.get());
    }

    @GET
    @Path("/view_freemarker")
    @UnitOfWork
    @Produces(MediaType.TEXT_HTML)
    public PersonView getPersonViewFreemarker(@PathParam("personId") LongParam personId) {
        return new PersonView(PersonView.Template.FREEMARKER, findSafely(personId.get()));
    }

    @GET
    @Path("/view_mustache")
    @UnitOfWork
    @Produces(MediaType.TEXT_HTML)
    public PersonView getPersonViewMustache(@PathParam("personId") LongParam personId) {
        return new PersonView(PersonView.Template.MUSTACHE, findSafely(personId.get()));
    }

    private Person findSafely(long personId) {
    	final Optional<Person> person;
		
    	if (peopleDAO!=null) {
    		logger.info("Using Non-Injected peopleDAO");
    		person = peopleDAO.findById(personId);
    	} else {
    		logger.info("Using Injected peopleDAOProvider");
    		person = peopleDAOProvider.get().findById(personId);
    	}
        
        if (!person.isPresent()) {
            throw new NotFoundException("No such user.");
        }
        return person.get();
    }
}
