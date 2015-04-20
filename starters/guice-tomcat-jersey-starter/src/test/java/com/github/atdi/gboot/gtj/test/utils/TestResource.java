package com.github.atdi.gboot.gtj.test.utils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    @GET
    public Response getTestMap() {
        Map<String, String> entity = new HashMap<>();
        entity.put("user", "Test");
        entity.put("email", "test@email.com");
        return Response.ok(entity).build();
    }
}
