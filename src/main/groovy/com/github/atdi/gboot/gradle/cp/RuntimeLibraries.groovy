package com.github.atdi.gboot.gradle.cp

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact

/**
 * Copyright (C) 2015 Aurel Avramescu
 */
class RuntimeLibraries {

    private Project project

    private Set<ResolvedArtifact> getLibraries(String configurationName) {
        Configuration configuration = (configurationName == null ? null : this.project
                .getConfigurations().findByName(configurationName))
        if (configuration == null) {
            return null;
        }
        return configuration.getResolvedConfiguration()
                .getResolvedArtifacts()

    }
}
