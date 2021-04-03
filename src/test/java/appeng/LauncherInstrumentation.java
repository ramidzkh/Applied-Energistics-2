package appeng;

import cpw.mods.modlauncher.Launcher;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModWorkManager;
import net.minecraftforge.fml.loading.FMLLoader;
import org.assertj.core.util.Lists;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.SelectorResolutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.EngineDiscoveryResult;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

public class LauncherInstrumentation implements LauncherDiscoveryListener {

    @Override
    public void launcherDiscoveryStarted(LauncherDiscoveryRequest request) {

        // This is generated in Gradle and contains the same variables that would be passed to
        // runClient as environment variables otherwise, plus the working directory.
        Properties p = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/appeng_test.properties")) {
            if (in == null) {
                throw new IllegalStateException("The Gradle-generated properties file is missing, check the generateTestRunSettings task");
            }
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("The Gradle-generated properties file could not be read, check the generateTestRunSettings task");
        }

        System.setProperty("fml.earlyprogresswindow", "false");
        System.setProperty("MOD_CLASSES", p.getProperty("MOD_CLASSES"));
        System.setProperty("forge.logging.console.level", "debug");

        Launcher.main("-launchTarget", "junit",
                "-gameDir", p.getProperty("WORKING_DIR"),
                "-fml.forgeVersion", p.getProperty("FORGE_VERSION"),
                "-fml.mcVersion", p.getProperty("MC_VERSION"),
                "-fml.mcpVersion", p.getProperty("MCP_VERSION"));
    }

    @Override
    public void launcherDiscoveryFinished(LauncherDiscoveryRequest request) {
    }

    @Override
    public void engineDiscoveryStarted(UniqueId engineId) {
    }

    @Override
    public void engineDiscoveryFinished(UniqueId engineId, EngineDiscoveryResult result) {
    }

    @Override
    public void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
    }

}
