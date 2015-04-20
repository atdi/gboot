package com.github.atdi.gboot.guj;

import com.github.atdi.gboot.common.guice.GBootApplication;
import com.github.atdi.gboot.guj.test.utils.JerseyResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.Assert.*;

public class GujApplicationTest {

    private GBootApplication application;

    private Client client;

    @Before
    public void setUp() throws Exception {
        application = new GujApplication<>(JerseyResourceConfig.class.getCanonicalName(), null, null);
        application.start();
        client = ClientBuilder.newClient();
    }

    @After
    public void tearDown() throws Exception {
        application.stop();
    }

    @Test
    public void testGet() {
        WebTarget webTarget = client.target("http://localhost:8000/api/test");
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<String, String> entity = response.readEntity(Map.class);
        assertNotNull(entity);
        assertEquals("Test", entity.get("user"));
    }
}