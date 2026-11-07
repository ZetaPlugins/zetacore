package com.zetaplugins.zetacore.scan.inheritance;

import com.zetaplugins.zetacore.scan.fixtures.Marker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Inheritance {
    private Inheritance() {
    }

    @Marker
    public static class Base {}

    public static class Child extends Base {}

    public static class GrandChild extends Child {}

    @Marker
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MetaMarked {}

    @MetaMarked
    public static class ViaMeta {}

    public static class Unrelated {}
}
