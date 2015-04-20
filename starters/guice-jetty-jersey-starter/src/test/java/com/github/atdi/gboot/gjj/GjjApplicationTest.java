package com.github.atdi.gboot.gjj;

import com.github.atdi.gboot.common.guice.GBootApplication;
import com.github.atdi.gboot.gjj.tests.utils.JerseyResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GjjApplicationTest {

    private GBootApplication application;

    @Before
    public void setUp() throws Exception {
        application = new GjjApplication<>(JerseyResourceConfig.class.getCanonicalName(), null, null);
        application.start();
    }

    @After
    public void tearDown() throws Exception {
        application.stop();
    }

    @Test
    public void testGet() {

    }

}