package com.example.helloworld.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.example.helloworld.core.Person;
import com.google.common.base.Optional;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class PersonJPADAO {
	
	@com.google.inject.Inject
	private Provider<EntityManager> emProvider;
	
	@Transactional(rollbackOn=Exception.class)
	public Optional<Person> findById(Long id) {
		EntityManager em = emProvider.get();
		return Optional.fromNullable(em.find(Person.class, id));
    }

	@Transactional(rollbackOn=Exception.class)
    public Person create(Person person) {
    	EntityManager em = emProvider.get();
        em.persist(person);
        return person;
    }

	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn=Exception.class)
    public List<Person> findAll() {
		EntityManager em = emProvider.get();
		Query q = em.createQuery("select i from Person i");
        return (List<Person>)q.getResultList();
    }

}
