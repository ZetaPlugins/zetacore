package com.zetaplugins.zetacore.permission.fixtures;

import com.zetaplugins.zetacore.permission.Permission;
import com.zetaplugins.zetacore.permission.PermissionHolder;

@PermissionHolder
public final class HolderPermissions {
    public static final Permission ADMIN = Permission.of("test.admin");
    public static final Permission DUPLICATE = Permission.of("test.fly");
}
