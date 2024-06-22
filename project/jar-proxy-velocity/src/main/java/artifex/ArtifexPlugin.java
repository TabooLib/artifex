package artifex;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import ink.ptms.artifex.Proxy;
import ink.ptms.artifex.script.Script;
import ink.ptms.artifex.script.ScriptProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Artifex
 * artifex.ArtifexPlugin
 *
 * @author scorez
 * @since 4/21/24 14:26.
 */
@Plugin(id = "@plugin_id@")
public class ArtifexPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private static ScriptProject runningProject;

    @Inject
    public ArtifexPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void init(ProxyInitializeEvent e) {
        startup();
    }

    @Subscribe
    public void shutdown(ProxyShutdownEvent e) {
        shutdown();
    }

    private void startup() {
        try (InputStream resource = getClass().getResourceAsStream("META-INF/src.zip")) {
            if (resource != null) {
                ScriptProject project = Proxy.INSTANCE.readToScriptProject(resource).load();
                if (Proxy.INSTANCE.runProject(project)) {
                    runningProject = project;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void shutdown() {
        if (runningProject != null) {
            Proxy.INSTANCE.releaseProject(runningProject);
        }
    }

    @Nullable
    public static Object invoke(@NotNull String name, @NotNull String method, @NotNull Object... args) {
        for (Script script : runningProject.runningScripts()) {
            if (script.baseId().equals(name)) {
                return script.invoke(method, args);
            }
        }
        return null;
    }

    @NotNull
    public static ScriptProject getRunningProject() {
        return runningProject;
    }
}
