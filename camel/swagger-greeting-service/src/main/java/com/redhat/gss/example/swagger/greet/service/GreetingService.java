package com.redhat.gss.example.swagger.greet.service;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/greetingservice")
@Api(value = "/greetingservice", description = "Simple Spring JAX-RS service with Swagger documentation")
public interface GreetingService {

    @Path("/greet")
    @GET
    @Produces("text/plain")
    @ApiOperation(
            value = "Get operation with String value",
            notes = "Returns Hello World!",
            response = String.class
    )
    String greet();

}
