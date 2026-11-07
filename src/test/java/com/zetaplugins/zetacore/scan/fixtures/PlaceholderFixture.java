package com.zetaplugins.zetacore.scan.fixtures;

import com.zetaplugins.zetacore.integration.papi.annotation.Papi;

public class PlaceholderFixture {
    @Papi(identifier = "name")
    public String name() {
        return "fixture";
    }
}
