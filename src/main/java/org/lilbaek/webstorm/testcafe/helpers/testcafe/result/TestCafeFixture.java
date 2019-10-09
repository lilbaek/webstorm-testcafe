package org.lilbaek.webstorm.testcafe.helpers.testcafe.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCafeFixture {
    @JsonProperty("name")
    public String Name;

    @JsonProperty("path")
    public String Path;

    @JsonProperty("tests")
    public List<TestCafeTest> Tests;
}
