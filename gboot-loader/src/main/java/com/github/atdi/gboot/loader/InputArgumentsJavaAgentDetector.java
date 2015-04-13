package com.github.atdi.gboot.loader;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copyright (C) 2015 Aurel Avramescu
 */
public class InputArgumentsJavaAgentDetector implements JavaAgentDetector {

    private static final String JAVA_AGENT_PREFIX = "-javaagent:";

    private final Set<URI> javaAgentJars;

    public InputArgumentsJavaAgentDetector() {
        this(getInputArguments());
    }

    InputArgumentsJavaAgentDetector(List<String> inputArguments) {
        this.javaAgentJars = getJavaAgentJars(inputArguments);
    }

    private static List<String> getInputArguments() {
        try {
            return AccessController.doPrivileged(new PrivilegedAction<List<String>>() {
                @Override
                public List<String> run() {
                    return ManagementFactory.getRuntimeMXBean().getInputArguments();
                }
            });
        }
        catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private Set<URI> getJavaAgentJars(List<String> inputArguments) {
        Set<URI> javaAgentJars = new HashSet<URI>();
        for (String argument : inputArguments) {
            String path = getJavaAgentJarPath(argument);
            if (path != null) {
                try {
                    javaAgentJars.add(new File(path).getCanonicalFile().toURI());
                }
                catch (IOException ex) {
                    throw new IllegalStateException(
                            "Failed to determine canonical path of Java agent at path '"
                                    + path + "'");
                }
            }
        }
        return javaAgentJars;
    }

    private String getJavaAgentJarPath(String arg) {
        if (arg.startsWith(JAVA_AGENT_PREFIX)) {
            String path = arg.substring(JAVA_AGENT_PREFIX.length());
            int equalsIndex = path.indexOf('=');
            if (equalsIndex > -1) {
                path = path.substring(0, equalsIndex);
            }
            return path;
        }
        return null;
    }

    @Override
    public boolean isJavaAgentJar(URI url) {
        return this.javaAgentJars.contains(url);
    }

}