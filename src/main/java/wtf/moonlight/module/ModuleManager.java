/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package wtf.moonlight.module;

import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wtf.moonlight.Client;
import com.cubk.EventTarget;
import wtf.moonlight.events.misc.KeyPressEvent;
import wtf.moonlight.module.impl.combat.*;
import wtf.moonlight.module.impl.display.*;
import wtf.moonlight.module.impl.display.ArrayList;
import wtf.moonlight.module.impl.misc.Timer;
import wtf.moonlight.module.impl.misc.*;
import wtf.moonlight.module.impl.movement.*;
import wtf.moonlight.module.impl.player.*;
import wtf.moonlight.module.impl.visual.*;

import java.util.*;

/**
 * Manages all modules within the MoonLight client.
 * Responsible for initializing, registering, and handling modules.
 */
public final class ModuleManager {

    // Sort modules alphabetically by name for better organization
    private static final Comparator<Module> MODULE_COMPARATOR = Comparator.comparing(Module::getName);

    private final Collection<Module> modules = new TreeSet<>(MODULE_COMPARATOR);
    private final Map<Class<? extends Module>, Module> registry = new HashMap<>(128);
    private final Map<Categor, Set<Module>> categories = new EnumMap<>(Categor.class);

    /**
     * Initializes the ModuleManager by adding all available modules,
     * sorting them by name, and registering event listeners.
     */
    public ModuleManager() {
        // Init default sets
        for (Categor category : Categor.values()) {
            categories.put(category, new TreeSet<>(MODULE_COMPARATOR));
        }

        addModules(
                // Combat
                AntiBot.class,
                AutoGap.class,
                AutoPotion.class,
                AutoProjectile.class,
                BackTrack.class,
                BowAimBot.class,
                Critical.class,
                KeepSprint.class,
                KillAura.class,
                TargetStrafe.class,
                TickBase.class,
                Velocity.class,
                AutoRod.class,

                // Misc
                Blink.class,
                ClientSpoofer.class,
                Disabler.class,
                FakeLag.class,
                NoRotate.class,
                Timer.class,
                AutoAuthenticate.class,
                AutoPlay.class,
                HackerDetector.class,
                KillSults.class,
                RawMouseInput.class,

                // Movement
                AntiFall.class,
                Freeze.class,
                InvMove.class,
                LongJump.class,
                NoJumpDelay.class,
                NoSlowdown.class,
                Phase.class,
                SafeWalk.class,
                Speed.class,
                Sprint.class,
                Step.class,
                Strafe.class,
                Fly.class,
                Scaffold.class,
                NoFluid.class,
                NoWeb.class,

                // Player
                AntiFireball.class,
                AutoPearl.class,
                AutoTool.class,
                FastPlace.class,
                InvManager.class,
                NoFall.class,
                ChestStealer.class,
                BedNuker.class,

                // Visual
                Atmosphere.class,
                Animations.class,
                AspectRatio.class,
                AttackEffect.class,
                BedPlates.class,
                BlockOverlay.class,
                Camera.class,
                Chams.class,
                ChestESP.class,
                ClickGUI.class,
                DashTrail.class,
                DeadEffect.class,
                ESP2D.class,
                Particles.class,
                FreeLook.class,
                FullBright.class,
                GlowESP.class,
                ChinaHat.class,
                MotionBlur.class,
                HitBubbles.class,
                JumpCircles.class,
                LineGlyphs.class,
                SilentView.class,
                PostProcessing.class,
                Trajectories.class,
                TargetESP.class,
                Breadcrumbs.class,
                DamageParticles.class,
                ItemESP.class,
                NameHider.class,
                EnchantGlint.class,

                // display
                Interface.class,
                ArrayList.class,
                TargetHUD.class,
                KeyBindHUD.class,
                Watermark.class,
                PotionHUD.class
        );

        // Register the ModuleManager to listen for events
        Client.INSTANCE.getEventManager().register(this);
        //  Moonlight.LOGGER.INFO("ModuleManager initialized with {} modules.", modules.size());
    }

    /**
     * Adds multiple modules to the manager by instantiating their classes.
     *
     * @param moduleClasses Varargs of module classes to add.
     */
    @SafeVarargs
    public final void addModules(Class<? extends Module>... moduleClasses) {
        for (final var moduleClass : moduleClasses) {
            try {
                Module module = moduleClass.getDeclaredConstructor().newInstance();
                modules.add(module);
                registry.put(moduleClass, module);
                Categor category = moduleClass.getAnnotation(ModuleInfo.class).category();
                Set<Module> categoryModules = categories.get(category);
                categoryModules.add(module);
                categories.put(category, categoryModules);
                //  Moonlight.LOGGER.INFO("Added module: {}", module.getName());
            } catch (Exception e) {
                Client.LOGGER.error("Failed to instantiate module: {}", moduleClass.getSimpleName(), e);
            }
        }
    }

    /**
     * Retrieves a module instance based on its class type.
     *
     * @param moduleClass The class of the module to retrieve.
     * @param <T>         The type of the module.
     * @return An instance of the requested module or null if not found.
     */
    @NotNull
    public <T extends Module> T getModule(Class<T> moduleClass) {
        return (T) registry.get(moduleClass);
    }

    /**
     * Retrieves a module instance based on its name.
     *
     * @param name The name of the module to retrieve.
     * @return The module instance if found, otherwise null.
     */
    @Nullable
    public Module getModule(String name) {
        return CollectionsKt.firstOrNull(modules, m -> m.getName().equalsIgnoreCase(name));
    }

    /**
     * Retrieves all modules that belong to a specific category.
     *
     * @param category The category to filter modules by.
     * @return A list of modules within the specified category.
     */
    @NotNull
    public Set<Module> getModulesByCategory(Categor category) {
        return categories.get(category);
    }

    /**
     * Event handler for key press events.
     * Toggles the corresponding module if its keybind matches the pressed key.
     *
     * @param event The key press event.
     */
    @EventTarget
    public void onKey(KeyPressEvent event) {
        for (Module module : modules) {
            if (module.getKeyBind() == event.getKey()) {
                module.toggle();
            }
        }
    }

    /**
     * Retrieves all modules managed by this manager.
     *
     * @return An unmodifiable list of all modules.
     */
    public Collection<Module> getModules() {
        return List.copyOf(modules);
    }
}