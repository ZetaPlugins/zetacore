package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.permission.Permission;
import org.bukkit.permissions.PermissionDefault;

public class Permissions {
    private Permissions() {}

    public static final Permission ROOT = Permission.of("testplugin");

    public static final Permission COUNT = ROOT.child("count", PermissionDefault.OP, "Allows using the /count command");
    public static final Permission DEBUG = ROOT.child("debug", PermissionDefault.OP, "Allows using the /debug command");
    public static final Permission MAN = ROOT.child("man", PermissionDefault.OP, "Allows using the /man command");
}
