package org.lilbaek.webstorm.testcafe.helpers;

import java.util.List;

public class TestCafeUiSession {

    private List<String> myCommandLineArguments;
    private TestCafeTestResultFinderStrategy myTestResultFinderStrategy;

    private TestCafeUiSession(List<String> commandLineArguments, TestCafeTestResultFinderStrategy testResultFinderStrategy) {
        myCommandLineArguments = commandLineArguments;
        myTestResultFinderStrategy = testResultFinderStrategy;
    }

    public static TestCafeUiSession create(List<String> commandLineArguments, TestCafeTestResultFinderStrategy testResultFinderStrategy) {
        return new TestCafeUiSession(commandLineArguments, testResultFinderStrategy);
    }

    public TestCafeTestResultFinderStrategy getTestResultFinderStrategy() {
        return myTestResultFinderStrategy;
    }

    public List<String> getCommandLineArguments() {
        return myCommandLineArguments;
    }
}
