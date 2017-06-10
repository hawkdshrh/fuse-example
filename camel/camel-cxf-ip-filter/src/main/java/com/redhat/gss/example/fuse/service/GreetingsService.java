package com.redhat.gss.example.fuse.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(targetNamespace = "com.redhat.gss.example.fuse.service", name = "simplews")
public interface GreetingsService {

    @WebMethod(operationName = "sayHello")
    public @WebResult(name = "Greeting") String sayHello(@WebParam(name = "name") String name);
}
