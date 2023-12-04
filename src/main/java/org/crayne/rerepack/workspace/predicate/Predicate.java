package org.crayne.rerepack.workspace.predicate;

import org.jetbrains.annotations.NotNull;

public class Predicate<T> {

    @NotNull
    private final T t1, t2;

    public Predicate(@NotNull final T t1, @NotNull final T t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    @NotNull
    public T key() {
        return t1;
    }

    @NotNull
    public T value() {
        return t2;
    }

    @NotNull
    public String toString() {
        return t1 + " = " + t2;
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Predicate<?> predicate = (Predicate<?>) o;

        if (!t1.equals(predicate.t1)) return false;
        return t2.equals(predicate.t2);
    }

    public int hashCode() {
        int result = t1.hashCode();
        result = 31 * result + t2.hashCode();
        return result;
    }
}
