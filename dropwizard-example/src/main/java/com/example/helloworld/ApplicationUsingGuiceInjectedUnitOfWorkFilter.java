package com.example.helloworld;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.helloworld.db.PersonJPADAO;
import com.example.helloworld.filter.UnitOfWorkFilterGuiceInjected;
import com.example.helloworld.resources.PeopleResource;
import com.example.helloworld.resources.PersonResource;
import com.example.helloworld.util.StartHelper;
import com.example.helloworld.util.StaticUtil;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.soabase.guice.GuiceBundle;
import io.soabase.guice.InjectorProvider;
import io.soabase.guice.JerseyGuiceModule;
import io.soabase.guice.StandardInjectorProvider;

public class ApplicationUsingGuiceInjectedUnitOfWorkFilter extends Application<HelloWorldConfiguration> {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationUsingGuiceInjectedUnitOfWorkFilter.class);
	
	private InjectorProvider<HelloWorldConfiguration> injectorProvider;
	private Injector injector;
	private static String confFile;
	
    public static void main(String[] args) throws Exception {
    	confFile = args[args.length-1];
		System.out.println("Using config : " + confFile);
        new ApplicationUsingGuiceInjectedUnitOfWorkFilter().run(args);
    }
    
    public Injector getInjector() {
    	return injector;
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
    	
    	HelloWorldConfiguration configuration;
    	
		if (confFile==null) {
			setConfFile();
		} else {
			System.out.println("Using Config File " + confFile);
		}
		
		configuration = StaticUtil.getConfiguration(confFile);
		
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        
        JpaPersistModule jpaModule = getJpaPersistModule(configuration);
        
        injectorProvider = new StandardInjectorProvider<>(
        		new DAOGuiceModule(),
        		jpaModule,
				new JerseyGuiceModule(){
					@Override
					protected void configureServlets() {
						bind(PersonResource.class);
//						bind(UnitOfWorkFilter.class);
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
    }

	private void setConfFile() {
		System.out.println("Config File is null");
		if (StartHelper.getConfigurationFile()==null) {
			throw new IllegalStateException();
		}
		confFile = StartHelper.getConfigurationFile();
		System.out.println("Got Config File from StartHelper " + confFile);
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
        
        register(environment, PeopleResource.class.getCanonicalName());
        register(environment, UnitOfWorkFilterGuiceInjected.class.getCanonicalName());
		
		environment.lifecycle().manage(new Managed() {
			
            @Override
            public void start() {
            	injector.getInstance(PersistService.class).start();
            }

            @Override
            public void stop() {
                injector.getInstance(PersistService.class).stop();
            }
        });
    }
    
    private void register(Environment environment, String name) {
		try {
			Class<?> c = Class.forName(name);
			Constructor<?> ctor = c.getConstructor();
			Object instanceOfTheClass = ctor.newInstance();
			injector.injectMembers(instanceOfTheClass);
			environment.jersey().register(instanceOfTheClass);
			logger.info("Registered Jersey Resource : {}", name);
			System.out.println("Registered Jersey Resource : " + name);
		} catch (Exception e) {
			logger.error("FATAL: Couldn't initialize Jersey Resource :{}", name, e);
			throw new IllegalStateException(e);
		}
	}
    
    private JpaPersistModule getJpaPersistModule(HelloWorldConfiguration configuration) {
		Properties properties = new Properties();
		Map<String, String> dbProperties = configuration.getDataSourceFactory().getProperties();
		properties.putAll(dbProperties);
		JpaPersistModule persistModule = new JpaPersistModule("test-persistence-unit");
		persistModule.properties(properties);
		return persistModule;
	}
    
    private static class DAOGuiceModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(PersonJPADAO.class);
		}
    }
    
    public static void printConfig(HelloWorldConfiguration configuration) {
		System.out.println("Loaded Application Configuration: " + ToStringBuilder.reflectionToString(configuration, ToStringStyle.MULTI_LINE_STYLE));
		System.out.println("Loaded serverFactory : " +  new ReflectionToStringBuilder(configuration.getServerFactory(), new RecursiveToStringStyle()).toString());
		System.out.println("Loaded DataSourceFactory : " + ToStringBuilder.reflectionToString(configuration.getDataSourceFactory(), ToStringStyle.MULTI_LINE_STYLE));
	}
}
