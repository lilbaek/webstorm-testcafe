package org.lilbaek.webstorm.testcafe.helpers;

import org.jetbrains.annotations.Nullable;
import org.lilbaek.webstorm.testcafe.helpers.testcafe.result.TestCafeJson;

public interface TestCafeTestResultFinderStrategy {

    /**
     * Attempt to find test results corresponding to the most recent testcafe invocation.
     */
    @Nullable
    TestCafeJson findTestResults();

    /** Remove any temporary files used by this result finder. */
    void deleteTemporaryOutputFiles();
}
