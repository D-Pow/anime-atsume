package org.animeatsume.gradleutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @see <a href="https://docs.gradle.org/current/userguide/implementing_gradle_plugins.html">Custom Gradle plugins</a>
 * @see <a href="https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html">Gradle Plugins docs</a>
 */
public class AnimeAtsumeBuildUtils implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        // no-op
    }
}
