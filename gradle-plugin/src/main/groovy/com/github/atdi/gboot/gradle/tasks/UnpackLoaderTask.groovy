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
package com.github.atdi.gboot.gradle.tasks

import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.Copy


class UnpackLoaderTask extends Copy {

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
