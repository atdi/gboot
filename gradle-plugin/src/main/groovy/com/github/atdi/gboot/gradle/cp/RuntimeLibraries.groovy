package com.github.atdi.gboot.gradle.cp

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact

/**
 * Copyright (C) 2015 Aurel Avramescu
 */
class RuntimeLibraries {

    private Project project

    private Set<String> getLibraries(String configurationName) {
        Set<String> libNames = new HashSet<>();
        Configuration configuration = (configurationName == null ? null : this.project
                .getConfigurations().findByName(configurationName))
        if (configuration == null) {
            return null;
        }
        for (ResolvedArtifact artifact : configuration
                .getResolvedConfiguration()
                .getResolvedArtifacts()) {
            libNames.add(String.format("%s-%s.%s",
                    artifact.getName(),
                    artifact.getModuleVersion().getId().getVersion(),
                    artifact.getExtension()))
        }

        return libNames

    }
}
