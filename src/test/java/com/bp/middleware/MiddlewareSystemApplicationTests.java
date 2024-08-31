package com.bp.middleware;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.bp.middleware.person.PersonRepo;
import com.bp.middleware.person.PersonService;

@SpringBootTest
class MiddlewareSystemApplicationTests {

	@Mock
	private PersonRepo personRepo;

	private PersonService personService;

	@BeforeEach
	void setUp() {
		this.personService = new PersonService(this.personRepo);
	}

	@Test
	void getAllPerson() {
		personService.getAllPerson();
		verify(personRepo).findAll();
	}
}
