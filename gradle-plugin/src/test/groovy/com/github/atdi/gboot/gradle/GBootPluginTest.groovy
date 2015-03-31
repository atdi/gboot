package com.github.atdi.gboot.gradle

import static org.junit.Assert.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

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
    void testJarFail() {
        project.tasks.jar.execute()
    }

    @Test
    void testJarPass() {
        project.gBoot.mainClass = "com.play.Main"
        project.tasks.jar.execute()
        assertTrue(project.tasks.jar.getState().executed)
    }

    @Test
    void testJarPassWithLoaderConf() {
        project.gBoot.mainClass = "com.play.Main"
        project.dependencies {
            loader 'org.projectlombok:lombok:1.16.2'
        }
        project.tasks.unpackLoader.execute()
        project.tasks.jar.execute()
        assertTrue(project.tasks.unpackLoader.getState().executed)
        assertTrue(project.tasks.jar.getState().executed)
        assertTrue(project.file(project.buildDir.absolutePath + "/classes/main/lombok").exists())

    }


    private setupProject() {
        Project project = ProjectBuilder.builder()
                .withName('test-project')
                .build()
        project.apply plugin: 'com.github.atdi.gboot'
        project.apply plugin: 'java'
        project.repositories {
            mavenLocal()
            mavenCentral()
        }
        project.dependencies {
            compile("org.eclipse.jetty:jetty-server:9.2.10.v20150310")
            testCompile("junit:junit:4.12")
        }

        return project
    }
}
