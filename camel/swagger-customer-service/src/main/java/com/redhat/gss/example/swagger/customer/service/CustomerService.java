/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.redhat.gss.example.swagger.customer.service;

import com.redhat.gss.example.swagger.customer.Customer;
import com.wordnik.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/customer/")
@Api(value = "/customer", description = "Operations about customerservice")
public interface CustomerService {

    @GET
    @Path("/{id}/")
    @Produces({ "application/xml", "application/json"})
    @ApiOperation(value = "Find Customer by ID", notes = "More notes about this method", response = Customer.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Invalid ID supplied"),
            @ApiResponse(code = 204, message = "Customer not found")
    })
    public Response getCustomer(@ApiParam(value = "ID of Customer to fetch", required = true) @PathParam("id") String id);

    @POST
    @Path("/update/{id}/")
    @Consumes({"application/xml", "application/json"})
    @ApiOperation(value = "Update an existing Customer")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Invalid ID supplied"),
            @ApiResponse(code = 204, message = "Customer not found")
    })
    public Response updateCustomer(@ApiParam(value = "Customer object that needs to be updated", required = true) Customer customer);

    @POST
    @Path("/")
    @Consumes({"application/xml", "application/json"})
    @ApiOperation(value = "Add a new Customer")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Invalid ID supplied"),})
    public Response addCustomer(@ApiParam(value = "Customer object that needs to be updated", required = true) Customer customer);

    @DELETE
    @Path("/delete/{id}/")
    @ApiOperation(value = "Delete Customer")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Invalid ID supplied"),
            @ApiResponse(code = 204, message = "Customer not found")
    })
    public Response deleteCustomer(@ApiParam(value = "ID of Customer to delete", required = true) @PathParam("id") String id);
}