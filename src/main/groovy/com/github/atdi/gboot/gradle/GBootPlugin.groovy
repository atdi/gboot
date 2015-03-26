package com.github.atdi.gboot.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Copyright (C) 2015 Aurel Avramescu.
 */
class GBootPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.getExtensions().create("gBoot", GBootPluginExtension)

        project.getPlugins().apply(JavaPlugin)

        project.tasks.jar {
            into('lib') {
                from project.configurations.runtime
            }
        }

    }


}
