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

import com.github.atdi.gboot.gradle.tasks.UnpackLoaderTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy


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

        project.task("unpackLoader", type: Copy) {
            from {
                project.configurations.loader.collect {
                    project.zipTree(it)
                }
            }
            into "$project.buildDir/classes/main/"
            exclude {"META-INF"}
        }

        project.tasks.jar {

            dependsOn project.tasks.unpackLoader

            doFirst {
                if (project.gBoot.mainClass == "") {
                    throw new GradleException("Please specify the main class")
                }
            }
            into('lib') {
                from project.configurations.runtime
            }
            entryCompression org.gradle.api.tasks.bundling.ZipEntryCompression.STORED
            manifest {
                attributes("Main-Class": "com.github.atdi.gboot.loader.JarLauncher",
                           "Start-Class": project.gBoot.mainClass)
            }
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
