package com.example.helloworld;

import com.google.inject.persist.PersistService;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import javax.inject.Inject;

public class PersistServiceShim {
  @Inject
  public PersistServiceShim(final PersistService service, Environment environment) {
      service.start();
      environment.lifecycle().manage(new Managed() {
            @Override
            public void start() {
            	// NOP
            }

            @Override
            public void stop() {
                service.stop();
            }
        });
  }
}
