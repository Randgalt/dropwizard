package com.example.helloworld;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.example.helloworld.core.Person;
import com.example.helloworld.util.StartHelper;
import com.example.helloworld.util.StaticUtil;

import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

public class IntegrationTestHK2InjectedUnitOfWorkFilter {

    public static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.yml");
    private static final HelloWorldConfiguration CONFIGURATION ; 

    static {
    	System.out.println("Setting config path");
    	StartHelper.setConfigurationFile(CONFIG_PATH);
    	CONFIGURATION = StaticUtil.getConfiguration(CONFIG_PATH);
    	System.out.println("Loaded config: " + ToStringBuilder.reflectionToString(CONFIGURATION));
    }
    
    public static final DropwizardTestSupport<HelloWorldConfiguration> TEST_SUPPORT = new DropwizardTestSupport<HelloWorldConfiguration>
	(ApplicationUsingHK2InjectedUnitOfWorkFilter.class,  CONFIGURATION);
	
	@ClassRule
	public static final DropwizardAppRule<HelloWorldConfiguration> RULE = new DropwizardAppRule<HelloWorldConfiguration>(TEST_SUPPORT);

    private Client client;

    @Before
    public void setUp() throws Exception {
        client = ClientBuilder.newClient();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void testPostPerson() throws Exception {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard");
        final Person newPerson = client.target("http://localhost:" + RULE.getLocalPort() + "/people")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Person.class);
        assertThat(newPerson.getId()).isNotNull();
        assertThat(newPerson.getFullName()).isEqualTo(person.getFullName());
        assertThat(newPerson.getJobTitle()).isEqualTo(person.getJobTitle());
    }
}
