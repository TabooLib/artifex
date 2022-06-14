package artifex;

import ink.ptms.artifex.controller.Proxy;
import ink.ptms.artifex.script.ScriptProject;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Artifex
 * artifex.ArtifexPlugin
 *
 * @author 坏黑
 * @since 2022/6/13 18:14
 */
public class ArtifexPlugin extends JavaPlugin {

    private static final List<ScriptProject> runningList = new ArrayList<>();

    @Override
    public void onEnable() {
        startup();
    }

    @Override
    public void onDisable() {
        shutdown();
    }

    private void startup() {
        InputStream resource = getResource("META-INF/src.zip");
        if (resource != null) {
            ScriptProject project = Proxy.INSTANCE.readToScriptProject(resource).load();
            if (Proxy.INSTANCE.runProject(project)) {
                runningList.add(project);
            }
        }
    }

    private void shutdown() {
        runningList.forEach(Proxy.INSTANCE::releaseProject);
    }

    @NotNull
    public static List<ScriptProject> getRunningList() {
        return runningList;
    }
}
