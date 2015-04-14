/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atdi.gboot.examples.guice.jetty.resteasy.resources;

import com.atdi.gboot.examples.guice.jetty.resteasy.model.User;
import com.atdi.gboot.examples.guice.jetty.resteasy.model.UserBuilder;
import com.atdi.gboot.examples.guice.jetty.resteasy.services.UserService;
import com.google.inject.servlet.RequestScoped;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * User rest resources.
 */
@RequestScoped
@Path("/users")
@Produces("application/json")
public class UserResource {

    @Inject
    private UserService userService;


    @POST
    @Consumes("application/json")
    public Response addUser(User user) {
        User copyUser = new UserBuilder().copy(user).withId(UUID.randomUUID()).build();
        return Response.status(Response.Status.CREATED).entity(userService.addUser(copyUser)).build();
    }

    @GET
    @Path("/{userId}")
    public Response getUser(@PathParam("userId") String userId) {
        return Response.ok().entity(userService.findById(UUID.fromString(userId))).build();
    }

    @PUT
    @Path("/{userId}")
    @Consumes("application/json")
    public Response updateUser(@PathParam("userId") String userId, User user) {
        return Response.status(Response.Status.CREATED).entity(userService.updateUser(user)).build();
    }

    @GET
    public Response getUsers() {
        return Response.ok().entity(userService.findUsers(null)).build();
    }


}
