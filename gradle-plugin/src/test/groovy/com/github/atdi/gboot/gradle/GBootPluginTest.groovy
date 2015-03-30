package com.github.atdi.gboot.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/**
 * Copyright (C) 2015 Aurel Avramescu.
 */
class GBootPluginTest extends GroovyTestCase {

    Project project

    void setUp() {
        super.setUp()
        project = setupProject()
    }

    void tearDown() {

    }

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
