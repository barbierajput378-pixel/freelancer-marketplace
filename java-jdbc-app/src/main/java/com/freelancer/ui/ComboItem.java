package com.freelancer.ui;

/**
 * One entry in a foreign-key dropdown: an id plus a human-readable label
 * (e.g. {@code "101 — Rahul Sharma"}). The combo shows {@link #toString()} but
 * callers read {@link #getId()} for the actual value. Equality is by id so that
 * {@code setSelectedItem(new ComboItem(id, null))} re-selects the right row.
 */
final class ComboItem {

    private final int id;
    private final String label;

    ComboItem(int id, String label) {
        this.id = id;
        this.label = label;
    }

    int getId() {
        return id;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ComboItem other) && other.id == this.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
