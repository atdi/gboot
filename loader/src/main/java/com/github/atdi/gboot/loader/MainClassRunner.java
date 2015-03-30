package com.github.atdi.gboot.loader;

import java.lang.reflect.Method;

/**
 * Copyright (C) 2015 Aurel Avramescu
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

    @Override
    public void run() {
        try {
            Class<?> mainClass = Thread.currentThread().getContextClassLoader()
                    .loadClass(this.mainClassName);
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            if (mainMethod == null) {
                throw new IllegalStateException(this.mainClassName
                        + " does not have a main method");
            }
            mainMethod.invoke(null, new Object[] { this.args });
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
