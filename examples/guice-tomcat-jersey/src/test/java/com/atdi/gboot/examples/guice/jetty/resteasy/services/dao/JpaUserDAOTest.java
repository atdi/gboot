package com.atdi.gboot.examples.guice.jetty.resteasy.services.dao;

import com.atdi.gboot.examples.guice.jetty.resteasy.model.User;
import com.atdi.gboot.examples.guice.jetty.resteasy.model.UserBuilder;
import com.atdi.gboot.examples.guice.jetty.resteasy.modules.PersistenceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class JpaUserDAOTest {

    @Inject
    private GenericDAO<User> dao;


    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new PersistenceModule("demo-guice-boot"));
        injector.injectMembers(this);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreate() throws Exception {
        UUID lastId = UUID.randomUUID();
        User user = new UserBuilder()
                .withId(lastId)
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email@email.com")
                .withPassword("password")
                .build();
        User retUser = dao.create(user);
        assertEquals(user, retUser);
        User user2 = new UserBuilder()
                .withId(UUID.randomUUID())
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email2@email.com")
                .withPassword("password")
                .build();
        User retUser2 = dao.create(user2);
        assertEquals(user2, retUser2);
    }

    @Test
    public void testUpdate() throws Exception {
        UUID lastId = UUID.randomUUID();
        User user = new UserBuilder()
                .withId(lastId)
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email3@email.com")
                .withPassword("password")
                .build();
        User retUser = dao.create(user);
        User userToUpdate = dao.findById(lastId);
        assertNotNull(user);
        user.setFirstName("UPdateFirst");
        User retUser2 = dao.update(userToUpdate);
        assertEquals("UPdateFirst", retUser2.getFirstName());
    }


    @Test
    public void testDelete() throws Exception {
        UUID lastId = UUID.randomUUID();
        User user = new UserBuilder()
                .withId(lastId)
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email4@email.com")
                .withPassword("password")
                .build();
        User retUser = dao.create(user);
        dao.delete(retUser);
        User userDeleted = dao.findById(lastId);
        assertNull(userDeleted);
    }

    @Test
    public void testFindByCriteria() throws Exception {
        UUID lastId = UUID.randomUUID();
        User user = new UserBuilder()
                .withId(lastId)
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email5@email.com")
                .withPassword("password")
                .build();
        dao.create(user);
        List<User> retUser = dao.findByCriteria(new UserBuilder().withEmail("email5@email.com").build());
        assertEquals(1, retUser.size());
    }

    @Test
    public void testFindAll() throws Exception {
        UUID lastId = UUID.randomUUID();
        User user = new UserBuilder()
                .withId(lastId)
                .withFirstName("FName")
                .withLastName("LName")
                .withEmail("email6@email.com")
                .withPassword("password")
                .build();
        dao.create(user);
        List<User> retUser = dao.findByCriteria(null);
        assertEquals(1, retUser.size());
    }
}