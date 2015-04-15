package com.atdi.gboot.examples.guice.jetty.jersey.resources;

import com.atdi.gboot.examples.guice.jetty.jersey.model.User;
import com.atdi.gboot.examples.guice.jetty.jersey.model.UserBuilder;
import com.atdi.gboot.examples.guice.jetty.jersey.modules.PersistenceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import java.util.UUID;

import static org.junit.Assert.*;

public class UserResourceTest {

    @Inject
    private UserResource userResource;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new PersistenceModule("demo-guice-boot"));
        injector.injectMembers(this);
    }

    @Test
    public void testAddUser() throws Exception {
        User user = new UserBuilder()
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email@email.com")
                .withPassword("password")
                .build();
        Response response = userResource.addUser(user);
        User retUser = (User)response.getEntity();
        assertNotNull(retUser);
        assertNotNull(retUser.getId());
    }

    @Test
    public void testGetUser() throws Exception {
        User user = new UserBuilder()
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email@email.com")
                .withPassword("password")
                .build();
        Response response = userResource.addUser(user);
        UUID retUserId = ((User)response.getEntity()).getId();
        response = userResource.getUser(retUserId.toString());
        User retUser = (User)response.getEntity();
        assertNotNull(retUser);
        assertEquals(retUserId, retUser.getId());
    }

    @Test
    public void testUpdateUser() throws Exception {
        User user = new UserBuilder()
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email@email.com")
                .withPassword("password")
                .build();
        Response response = userResource.addUser(user);
        User retUser = (User)response.getEntity();
        User userToUpdate = new UserBuilder().copy(retUser).withEmail("email2@email.com").build();
        response = userResource.updateUser(retUser.getId().toString(), userToUpdate);
        User updatedUser = (User)response.getEntity();
        assertEquals(userToUpdate, updatedUser);
    }

}