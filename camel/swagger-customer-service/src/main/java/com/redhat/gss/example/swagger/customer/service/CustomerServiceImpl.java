package com.redhat.gss.example.swagger.customer.service;

import com.redhat.gss.example.swagger.customer.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CustomerServiceImpl implements CustomerService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private static final Map<Long, Customer> customers = new HashMap<Long, Customer>();

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

        long idNumber = Long.parseLong(id);
        Customer c = customers.get(idNumber);

        if (c == null) {
            System.out.println("Customer is null: " + (c == null));
            response = Response.status(Response.Status.NOT_FOUND).entity("<error>Could not find customer</error>").build();
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
            r = Response.status(406).entity("Cannot find the customer!").build();
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

        long idNumber = Long.parseLong(id);
        Customer c = customers.get(idNumber);

        if (c == null) {
            System.out.println("Customer not found : " + (c == null));
            response = Response.notModified().build();
        } else {
            System.out.println("Customer deleted");
            customers.remove(idNumber);
            response = Response.ok().build();
        }

        return response;
    }
}
