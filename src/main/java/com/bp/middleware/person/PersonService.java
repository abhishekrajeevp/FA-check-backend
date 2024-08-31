package com.bp.middleware.person;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

	@Autowired 
	private PersonRepo repo;
	 
    public List<Person> getAllPerson()
    {
        return this.repo.findAll();
    }
 
    public PersonService(PersonRepo repo)
    {
        // this keyword refers to current instance
        this.repo = repo;
    }
}






