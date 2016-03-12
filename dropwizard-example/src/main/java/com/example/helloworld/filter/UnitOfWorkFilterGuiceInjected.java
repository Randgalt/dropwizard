package com.example.helloworld.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.UnitOfWork;

@Provider
public class UnitOfWorkFilterGuiceInjected implements ContainerRequestFilter, ContainerResponseFilter{
	private static final Logger logger = LoggerFactory.getLogger(UnitOfWorkFilterGuiceInjected.class);
	
	@com.google.inject.Inject
    private UnitOfWork unitOfWork;

    public UnitOfWorkFilterGuiceInjected() {
    	logger.info("UnitOfWorkFilter instantiated");
    }

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		/**
		 * You are free to call any @Transactional methods while a unit of work is in progress this way. 
		 * When end() is called, any existing session is closed and discarded. It is safe to call begin() 
		 * multiple times--if a unit of work is in progress, nothing happens. 
		 * Similarly, if one is ended calling end() returns silently. UnitOfWork is threadsafe and can be cached 
		 * for multiple uses or injected directly into singletons.
		 */
		
		/**
		 * When you request an entity manager outside of a unit of work guice persist will implicitly start the unit of work for you. 
		 * Unfortunately the isActive() on UnitOfWork is package private. And you cannot test if a unit of work is active.

			There are two ways to explicitly start and end a unit of work. You can use the UnitOfWork and the methods begin() and end(). 
			Also the @Transactional annotation starts a unit of work. @Transactional will also end the unit of work if and only if it started it.
		 */
		logger.debug("Starting unit of work");
		unitOfWork.begin();
		logger.debug("Started unit of work");
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		logger.debug("Ending unit of work");
		unitOfWork.end();
		logger.debug("Ended unit of work");
	}
}
