package com.github.atdi.gboot.loader;

import java.net.URI;

/**
 * A strategy for detecting Java agents
 *
 */
public interface JavaAgentDetector {

    /**
     * Returns {@code true} if {@code url} points to a Java agent jar file, otherwise
     * {@code false} is returned.
     * @param url The url to examine
     * @return if the URL points to a Java agent
     */
    public boolean isJavaAgentJar(URI url);

}
