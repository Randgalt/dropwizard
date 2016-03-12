package com.example.helloworld.util;

import java.io.File;

import javax.validation.Validation;

import com.example.helloworld.ApplicationUsingGuiceInjectedUnitOfWorkFilter;
import com.example.helloworld.HelloWorldConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;

public class StaticUtil {

	public static HelloWorldConfiguration getConfiguration(String configurationFile) {
		ObjectMapper mapper = Jackson.newObjectMapper();
		ConfigurationFactory<HelloWorldConfiguration> factory =
				new ConfigurationFactory<>(
						HelloWorldConfiguration.class,
						Validation.buildDefaultValidatorFactory().getValidator(),
						mapper,
						""
						);
		HelloWorldConfiguration configuration;
		try {
			File file = new File(configurationFile);
			if (!file.exists()) {
				throw new RuntimeException("File doesn't exist: " + configurationFile);
			}
			configuration = factory.build(file);
			ApplicationUsingGuiceInjectedUnitOfWorkFilter.printConfig(configuration);
			return configuration;
		} catch (Exception e) {
			System.out.println("FATAL: " + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
