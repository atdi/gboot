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
package com.github.atdi.gboot.common.guice.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Guice Servlet context listener.
 */
public class GuiceInjectorCreator {

    private static Injector injector;

    private GuiceInjectorCreator() {}

    /**
     * Static method for getting the parent injector
     * in the resource config class.
     * @return parent injector
     */
    public static Injector getParentInjector() {
        return injector;
    }

    public static void createInjector(Module... modules) {
        injector = Guice.createInjector(modules);
    }
}
