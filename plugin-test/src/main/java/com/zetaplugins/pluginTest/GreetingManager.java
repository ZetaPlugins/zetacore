package com.zetaplugins.pluginTest;

import com.zetaplugins.zetacore.annotations.InjectManager;
import com.zetaplugins.zetacore.annotations.Manager;

@Manager
public class GreetingManager {

    @InjectManager
    private CountManager countManager;

    public GreetingManager() {
        System.out.println("GreetingManager constructor called: " + this.hashCode());
    }

    public String getGreeting(String name) {
        countManager.getCounter(null); // Just to demonstrate dependency injection
        System.out.println("GreetingManager.getGreeting called for: " + name);
        return "Hello, " + name + "! This is GreetingManager.";
    }
}
