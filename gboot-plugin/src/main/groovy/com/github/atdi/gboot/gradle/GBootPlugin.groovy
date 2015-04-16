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
package com.github.atdi.gboot.gradle

import com.github.atdi.gboot.gradle.tasks.GBootRunTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.ZipEntryCompression


/**
 * Main plugin class.
 * @author aurel
 */
class GBootPlugin implements Plugin<Project> {

    public static final String GBOOT_RUN_TASK = 'gBootRun'

    public static final String UNPACK_LOADER_TASK = "unpackLoader"
    public static final String COMPILE_JAVA_TASK = 'compileJava'

    @Override
    void apply(Project project) {
        project.getExtensions().create("gBoot", GBootPluginExtension)

        project.getPlugins().apply(JavaPlugin)

        createDefaultConfigurations(project)

        Task unpackLoader = createUnpackLoaderTask(project)

        createGBootRunTask(project)

        overrideJarTask(project, unpackLoader)

    }

    private overrideJarTask(Project project, Task unpackLoader) {
        project.tasks.jar {

            dependsOn unpackLoader

            doFirst {
                if (project.gBoot.startClass == "") {
                    throw new GradleException("Please specify the main class")
                }

                manifest {
                    print "Manifest evaluation: " + project.gBoot.startClass
                    attributes("Main-Class": project.gBoot.mainClass,
                            "Start-Class": project.gBoot.startClass)
                }
            }

            into('lib') {
                from project.configurations.runtime
            }
            entryCompression ZipEntryCompression.STORED

        }
    }

    private createGBootRunTask(Project project) {
        Task gBootRunTask = project.tasks.create(GBOOT_RUN_TASK, GBootRunTask)
        gBootRunTask.description = 'Run your java application'
        gBootRunTask.dependsOn(UNPACK_LOADER_TASK)
        gBootRunTask.doFirst {
            main = project.gBoot.startClass
            classpath = project.sourceSets.main.runtimeClasspath
        }
    }

    private createUnpackLoaderTask(Project project) {
        Task unpackLoader = project.task(UNPACK_LOADER_TASK, type: Copy) {
            from {
                project.configurations.loader.collect {
                    project.zipTree(it)
                }

            }
            include '**/*.class'
            into "$project.buildDir/classes/main/"

            includeEmptyDirs = false
        }
        unpackLoader.description = 'Unpack the class loader jar specified for loader configuration.'
        unpackLoader.dependsOn(COMPILE_JAVA_TASK)
    }

    /**
     * Create project configurations.
     * @param project
     */
    private createDefaultConfigurations(Project project) {
        project.configurations {
            loader
        }
    }

}
