package com.zetaplugins.zetacore.services.updatechecker;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * A class representing a semantic version (MAJOR.MINOR.PATCH) with optional label.
 * Supports parsing from string, comparison, and string representation.
 */
public class SemanticVersion implements Comparable<SemanticVersion>, Serializable {
    private int major;
    private int minor;
    private int patch;
    private String label;

    /**
     * Constructs a SemanticVersion with major, minor and patch.
     * @param major The major version number
     * @param minor The minor version number
     * @param patch The patch version number
     */
    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.label = "";
    }

    /**
     * Constructs a SemanticVersion with major, minor, patch and label.
     * @param major The major version number
     * @param minor The minor version number
     * @param patch The patch version number
     * @param label The optional label (e.g. "beta", "rc1")
     */
    public SemanticVersion(int major, int minor, int patch, String label) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.label = label != null ? label : "";
    }

    /**
     * Parses a semantic version string in the form "MAJOR.MINOR.PATCH" with an optional "-label"
     * and optional leading "v" (or "V"), e.g. "1.2.3", "v1.2.3", "1.2.3-beta".
     * Throws IllegalArgumentException for null, empty, malformed, non-numeric, or negative parts.
     * @param versionString The semantic version string to parse
     */
    public SemanticVersion(String versionString) {
        if (versionString == null) throw new IllegalArgumentException("versionString must not be null");

        String s = versionString.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("versionString must not be empty");

        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1).trim();
            if (s.isEmpty()) throw new IllegalArgumentException("versionString has 'v' but no version numbers");
        }

        String[] mainAndLabel = s.split("-", 2);
        String main = mainAndLabel[0].trim();
        if (main.isEmpty()) throw new IllegalArgumentException("version core (major.minor.patch) is empty");

        String[] parts = main.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("version must have major, minor and patch (format: X.Y.Z)");

        try {
            this.major = Integer.parseInt(parts[0].trim());
            this.minor = Integer.parseInt(parts[1].trim());
            this.patch = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("major, minor and patch must be integer numbers", e);
        }

        if (this.major < 0 || this.minor < 0 || this.patch < 0) {
            throw new IllegalArgumentException("major, minor and patch must be non-negative");
        }

        this.label = (mainAndLabel.length > 1) ? mainAndLabel[1].trim() : "";
    }

    public int getMajor() {
        return major;
    }

    public SemanticVersion setMajor(int major) {
        this.major = major;
        return this;
    }

    public int getMinor() {
        return minor;
    }

    public SemanticVersion setMinor(int minor) {
        this.minor = minor;
        return this;
    }

    public int getPatch() {
        return patch;
    }

    public SemanticVersion setPatch(int patch) {
        this.patch = patch;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public SemanticVersion setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Checks if this version is greater than the other version.
     * @param other The other SemanticVersion to compare against
     * @return True if this version is greater than the other version, false otherwise
     */
    public boolean isGreaterThan(SemanticVersion other) {
        return this.compareTo(other) > 0;
    }

    /**
     * Checks if this version is less than the other version.
     * @param other The other SemanticVersion to compare against
     * @return True if this version is less than the other version, false otherwise
     */
    public boolean isLessThan(SemanticVersion other) {
        return this.compareTo(other) < 0;
    }

    @Override
    public int compareTo(@NotNull SemanticVersion o) {
        if (this.major != o.major) {
            return Integer.compare(this.major, o.major);
        }
        if (this.minor != o.minor) {
            return Integer.compare(this.minor, o.minor);
        }
        if (this.patch != o.patch) {
            return Integer.compare(this.patch, o.patch);
        }
        return this.label.compareTo(o.label);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (label != null && !label.isEmpty() ? "-" + label : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SemanticVersion other)) return false;
        return this.major == other.major
                && this.minor == other.minor
                && this.patch == other.patch
                && ((this.label == null && other.label == null) || (this.label != null && this.label.equals(other.label)));
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(major);
        result = 31 * result + Integer.hashCode(minor);
        result = 31 * result + Integer.hashCode(patch);
        result = 31 * result + (label == null ? 0 : label.hashCode());
        return result;
    }
}
