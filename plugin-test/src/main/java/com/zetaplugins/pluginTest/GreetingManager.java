package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.di.annotation.Manager;

@Manager
public class GreetingManager {

    public GreetingManager() {
        System.out.println("GreetingManager constructor called: " + this.hashCode());
    }

    public String getGreeting(String name) {
        System.out.println("GreetingManager.getGreeting called for: " + name);
        return "Hello, " + name + "! This is GreetingManager.";
    }
}
