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
package com.github.atdi.gboot.loader;

import java.lang.reflect.Method;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Main class runner.
 */
public class MainClassRunner implements Runnable {

    private final String mainClassName;

    private final String[] args;

    /**
     * Create a new {@link MainClassRunner} instance.
     * @param mainClass the main class
     * @param args incoming arguments
     */
    public MainClassRunner(String mainClass, String[] args) {
        this.mainClassName = mainClass;
        this.args = (args == null ? null : args.clone());
    }

    @SuppressFBWarnings({"DM_EXIT"})
    @Override
    public void run() {
        try {
            Class<?> mainClass = Thread.currentThread().getContextClassLoader()
                    .loadClass(this.mainClassName);
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, new Object[] { this.args });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
