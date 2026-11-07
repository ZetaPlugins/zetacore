package com.zetaplugins.zetacore.permission;

import com.zetaplugins.zetacore.scan.ClassScanner;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionDiscoveryTest {

    @Test
    void discoversEnumsAndHolderClasses() {
        ClassScanner scanner = new ClassScanner("com.zetaplugins.zetacore.permission.fixtures", getClass().getClassLoader());

        Set<String> values = PermissionRegistrar.discover(scanner).stream()
                .map(PermissionNode::getValue)
                .collect(Collectors.toSet());

        assertEquals(Set.of("test.fly", "test.heal", "test.admin"), values);
    }
}
