package org.lilbaek.webstorm.testcafe.helpers.testcafe.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCafeTest {
    @JsonProperty("name")
    public String Name;

    @JsonProperty("skipped")
    public boolean Skipped;

    @JsonProperty("durationMs")
    public long Duration;

    @JsonProperty("errs")
    public List<String> Errors;
}
