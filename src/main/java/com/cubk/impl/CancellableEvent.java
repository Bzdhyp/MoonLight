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
package com.cubk.impl;
/**
 * An abstract class for cancellable events.
 * This class implements the {@link Event} and {@link Cancellable} interfaces.
 */
public abstract class CancellableEvent implements Event, Cancellable {
    /**
     * A flag indicating whether the event has been cancelled.
     */
    private boolean cancelled;

    /**
     * Sets the cancellation status of the event.
     *
     * @param cancelled {@code true} to cancel the event, {@code false} to allow it.
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Checks if the event has been cancelled.
     *
     * @return {@code true} if the event is cancelled, {@code false} otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
