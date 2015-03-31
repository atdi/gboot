package com.github.atdi.gboot.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * Copyright (C) 2015 Aurel Avramescu.
 */
class GBootPluginTest {

    Project project

    @Before
    void setUp() {
        project = setupProject()
    }

    @After
    void tearDown() {

    }

    @Test(expected = GradleException.class)
    void testJar() {
        project.tasks.jar.execute()
    }

    private setupProject() {
        Project project = ProjectBuilder.builder()
                .withName('test-project')
                .build()
        project.apply plugin: 'com.github.atdi.gboot'
        project.apply plugin: 'java'
        project.repositories {
            mavenCentral()
        }
        project.dependencies {
            compile("org.eclipse.jetty:jetty-server:9.2.10.v20150310")
            testCompile("junit:junit:4.12")
        }

        return project
    }
}
