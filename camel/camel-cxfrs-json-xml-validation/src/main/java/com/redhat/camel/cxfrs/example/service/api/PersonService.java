package com.redhat.camel.cxfrs.example.service.api;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/")
public class PersonService {
    
    private static final Logger LOG = LoggerFactory.getLogger(PersonService.class);

    static Map<String, Person> people = new HashMap<>();
    
    public PersonService() {
        Person test = new Person("000-00-0000", "Doe", "John", "Lee", 25);
        people.put("000-00-0000", test);
        Person test2 = new Person("000-00-0001", "Doe", "Jane", "Lynne", 24);
        people.put("000-00-0001", test);
    }
    
    /**
     * @param id
     * @return the itemMap
     */
    @GET
    @Path("/find/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPerson(@PathParam("id")String id) {
        
        Response response;

        LOG.info("In getPersion() for " + id);

        Person person = people.get(id);

        if (person == null) {
            LOG.info("Person is null: " + (person == null));
            response = Response.noContent().status(204).build();
        } else {
            response = Response.status(Response.Status.OK).entity(person).build();
        }

        return response;
    }
    
    @POST
    @Path("/new")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response addPerson(Person person) {
        LOG.info("In addPersion() for " + person.getId());
        people.put(person.getId(), person);
        return Response.ok(person).build();
    }
    
}
