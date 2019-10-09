package org.lilbaek.webstorm.testcafe.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;
import org.lilbaek.webstorm.testcafe.helpers.testcafe.result.TestCafeJson;

import java.io.*;

/**
 * A strategy for locating results from a single test invocation (e.g. output JSON files).
 *
 */
public class TestCafeTestFinderStrategy implements TestCafeTestResultFinderStrategy {
    private static final Logger logger = Logger.getInstance(TestCafeTestFinderStrategy.class);

    private final File outputFile;

    public TestCafeTestFinderStrategy(File wantedOutputFile) {
        this.outputFile = wantedOutputFile;
    }

    @Nullable
    @Override
    public TestCafeJson findTestResults() {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(outputFile))) {
            return new ObjectMapper().readValue(inputStream, TestCafeJson.class);
        } catch (IOException e) {
            logger.warn(e);
        } finally {
            if (!outputFile.delete()) {
                logger.warn("Could not delete output file: " + outputFile);
            }
        }
        return null;
    }

    @Override
    public void deleteTemporaryOutputFiles() {

    }
}
