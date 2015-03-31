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
import org.gradle.api.plugins.JavaPlugin

/**
 * Main plugin class.
 * @author aurel
 */
class GBootPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.getExtensions().create("gBoot", GBootPluginExtension)

        project.getPlugins().apply(JavaPlugin)

        project.tasks.jar {
            doFirst {
                if (project.gBoot.mainClass == null) {
                    throw new GradleException("Please specify the main class")
                }
            }
            into('lib') {
                from project.configurations.runtime
            }
            entryCompression org.gradle.api.tasks.bundling.ZipEntryCompression.STORED
            manifest {
                attributes("Main-Class": "com.github.atdi.gboot.loader",
                           "Start-Class": project.gBoot.mainClass)
            }
        }

    }


}
