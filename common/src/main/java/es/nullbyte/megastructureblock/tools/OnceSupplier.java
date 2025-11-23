package es.nullbyte.megastructureblock.tools;

import java.util.function.Supplier;

/**
 * Supplier abstraction for a singleton object as it works on Minecraft registries.
 * On minecraft, registry objects are instantiated each time they are petitioned,
 * but rather the same object is given to all petitioned instances.
 * @param <T>
 */
public class OnceSupplier<T> implements Supplier<T> {
    private Supplier<T> value;
    private boolean isSet = false;

    public void set(Supplier<T> value) {
        if (isSet) {
            throw new IllegalStateException("Value has already been set and cannot be modified.");
        }
        this.value = value;
        this.isSet = true;
    }


    @Override
    public T get() {
        if (!isSet) {
            throw new IllegalStateException("Value has not been set yet.");
        }
        return value.get();
    }

    public Supplier<T> getSupplier() {
        if (!isSet) {
            throw new IllegalStateException("Value has not been set yet.");
        }
        return value;
    }

    public boolean isSet() {
        return isSet;
    }
}
