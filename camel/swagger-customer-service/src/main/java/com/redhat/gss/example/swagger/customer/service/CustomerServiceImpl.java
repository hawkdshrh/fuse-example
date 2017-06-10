package com.redhat.gss.example.swagger.customer.service;

import com.redhat.gss.example.swagger.customer.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class CustomerServiceImpl implements CustomerService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private static final Map<Long, Customer> customers = new HashMap<>();

    public CustomerServiceImpl() {
        init();
    }

    final void init() {
        Customer c = new Customer();
        c.setName("Charles");
        c.setId(100);
        customers.put(c.getId(), c);
    }

    @Override
    public Response getCustomer(String id) {
        
        Response response;

        LOG.info("----invoking getCustomer, Customer id is: " + id);

        Long idNumber = Long.parseLong(id);
        Customer c = customers.get(idNumber);

        if (c == null) {
            System.out.println("Customer is null: " + (c == null));
            response = Response.noContent().status(204).build();
        } else {
            response = Response.status(Response.Status.OK).entity(c).build();
        }

        return response;
    }

    @Override
    public Response updateCustomer(Customer customer) {
        Response r;
        Customer c = customers.get(customer.getId());
        if (c != null) {
            customers.put(customer.getId(), customer);
            r = Response.ok(customer).build();
        } else {
            r = Response.notModified().status(204).build();
        }

        return r;
    }

    @Override
    public Response addCustomer(Customer customer) {

        customers.put(customer.getId(), customer);

        return Response.ok(customer).build();
    }

    @Override
    public Response deleteCustomer(String id) {

        Response response;

        LOG.info("----invoking deleteCustomer, Customer id is: " + id);

        Long idNumber = Long.parseLong(id);
        
        if (customers.containsKey(idNumber)) {
            
            LOG.info("Customer deleted");
            customers.remove(idNumber);
            response = Response.ok().build();
           
        } else {
            
            LOG.info("Customer not found : " + (idNumber));
            response = Response.notModified().status(204).build();
        }

        return response;
    }
}
