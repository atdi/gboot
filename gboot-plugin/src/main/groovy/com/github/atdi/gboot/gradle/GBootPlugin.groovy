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

    @Override
    void apply(Project project) {
        project.getExtensions().create("gBoot", GBootPluginExtension)

        project.getPlugins().apply(JavaPlugin)

        createDefaultConfigurations(project)

        Task unpackLoader = createUnpackLoaderTask(project)

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

    private createUnpackLoaderTask(Project project) {
        project.task("unpackLoader", type: Copy) {
            from {
                project.configurations.loader.collect {
                    project.zipTree(it)
                }

            }
            include '**/*.class'
            into "$project.buildDir/classes/main/"

            includeEmptyDirs = false
        }
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
