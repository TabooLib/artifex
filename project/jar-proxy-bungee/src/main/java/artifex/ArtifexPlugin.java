package artifex;

import ink.ptms.artifex.Proxy;
import ink.ptms.artifex.script.Script;
import ink.ptms.artifex.script.ScriptProject;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Artifex
 * artifex.ArtifexPlugin
 *
 * @author 坏黑
 * @since 2022/6/13 18:14
 */
public class ArtifexPlugin extends Plugin {

    private static ScriptProject runningProject;

    @Override
    public void onEnable() {
        startup();
    }

    @Override
    public void onDisable() {
        shutdown();
    }

    private void startup() {
        try (InputStream resource = getResourceAsStream("META-INF/src.zip")) {
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
