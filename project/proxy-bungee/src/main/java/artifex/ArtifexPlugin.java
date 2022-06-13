package artifex;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ink.ptms.artifex.controller.Proxy;
import ink.ptms.artifex.script.Script;
import ink.ptms.artifex.script.ScriptMeta;
import ink.ptms.artifex.script.ScriptRuntimeProperty;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Artifex
 * artifex.ArtifexPlugin
 *
 * @author 坏黑
 * @since 2022/6/13 18:14
 */
public class ArtifexPlugin extends Plugin {

    private static final List<Script> runningScripts = new ArrayList<>();

    @Override
    public void onEnable() {
        startup();
    }

    @Override
    public void onDisable() {
        shutdown();
    }

    private void startup() {
        String str = Proxy.INSTANCE.readAll(Objects.requireNonNull(getResourceAsStream("artifex.json")));
        JsonObject json = new JsonParser().parse(str).getAsJsonObject();
        if (json.has("elements")) {
            for (JsonElement script : json.getAsJsonArray("elements")) {
                InputStream resource = getResourceAsStream("elements/" + script.getAsString());
                if (resource != null) {
                    ScriptMeta scriptMeta = Proxy.INSTANCE.readToScriptMeta(resource);
                    ScriptRuntimeProperty property = new ScriptRuntimeProperty();
                    property.setDefaultFileFinder(Proxy.INSTANCE.createScriptFileFinder(new Proxy.ResourceLoader() {
                        @NotNull
                        @Override
                        public File getFile() {
                            return ArtifexPlugin.this.getFile();
                        }
                        @Nullable
                        @Override
                        public InputStream getResource(@NotNull String name) {
                            return ArtifexPlugin.this.getResourceAsStream(name);
                        }
                    }));
                    Proxy.INSTANCE.evalScript(scriptMeta, property).thenAccept(runningScripts::add);
                }
            }
        }
    }

    private void shutdown() {
        runningScripts.forEach(i -> i.container().releaseSafely(true));
    }

    @Nullable
    public static Object invoke(@NotNull String name, @NotNull String method, @NotNull Object... args) {
        for (Script script : runningScripts) {
            if (script.baseId().equals(name)) {
                return script.invoke(method, args);
            }
        }
        return null;
    }

    @NotNull
    public static List<Script> getRunningScripts() {
        return runningScripts;
    }
}
