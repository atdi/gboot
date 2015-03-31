package com.github.atdi.gboot.gradle.tasks

import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.AbstractCopyTask

/**
 * Copyright (C) 2015 Aurel Avramescu
 */
class UnpackLoaderTask extends AbstractCopyTask{

    @Override
    protected CopyAction createCopyAction() {
        from { // use of closure defers evaluation until execution time
            project.configurations.loader.collect {
                project.zipTree(it)
            }
        }
        into "$project.buildDir/classes/main/"
        exclude {"META-INF"}
    }
}
