package com.github.atdi.gboot.gradle.cp

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/**
 * Copyright (C) 2015 Aurel Avramescu
 */
class RuntimeLibrariesTest extends GroovyTestCase {

    RuntimeLibraries libraries;

    void setUp() {
        super.setUp()
        Project project = setupProject()
        libraries = new RuntimeLibraries(project: project)
    }

    void tearDown() {

    }

    void testGetLibraries() {
        Set<String> libs = libraries.getLibraries("runtime")
        assertEquals(5, libs.size())
        for (String lib : libs) {
            assertTrue(lib.endsWith('.jar'))
        }
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
