package com.example.helloworld;
import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.resources.PeopleResource;
import com.example.helloworld.resources.PersonResource;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.soabase.guice.GuiceBundle;
import io.soabase.guice.InjectorProvider;
import io.soabase.guice.JerseyGuiceModule;
import io.soabase.guice.StandardInjectorProvider;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
	
	private InjectorProvider<HelloWorldConfiguration> injectorProvider;
	private Injector injector;
	
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    private static final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
            new HibernateBundle<HelloWorldConfiguration>(Person.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        
        injectorProvider = new StandardInjectorProvider<>(new DAOGuiceModule(),
				new JerseyGuiceModule(){
					@Override
					protected void configureServlets() {
					}
				});
        
		GuiceBundle<HelloWorldConfiguration> guiceBundle = new GuiceBundle<>(injectorProvider);
		bootstrap.addBundle(guiceBundle);

        bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
    }

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment) {
    	
    	injector = injectorProvider.get(configuration, environment,
				new AbstractModule() {
				@Override
				protected void configure() {
					bind(HelloWorldConfiguration.class).toInstance(configuration);
				}
			});
        
        PeopleResource peopleResource = new PeopleResource();
        injector.injectMembers(peopleResource);
		environment.jersey().register(peopleResource);
		
        PersonResource personResource = new PersonResource();
        injector.injectMembers(personResource);
		environment.jersey().register(personResource);
		
    }
    
    private static class DAOGuiceModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(PersonDAO.class).toProvider(PersonDAOProvider.class);
		}
    }
    
    private static class PersonDAOProvider implements Provider<PersonDAO> {
    	
	    public PersonDAO get() {
	    	return new PersonDAO(hibernateBundle.getSessionFactory());
	    }
    }
}
