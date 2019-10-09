package org.lilbaek.webstorm.testcafe.helpers.testcafe.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCafeJson {

    @JsonProperty("passed")
    public Integer Passed;

    @JsonProperty("total")
    public Integer Total;

    @JsonProperty("fixtures")
    public List<TestCafeFixture> Fixtures;

}

