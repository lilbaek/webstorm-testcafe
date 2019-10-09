package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.execution.testframework.sm.runner.OutputEventSplitter;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.events.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.util.Key;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageVisitor;
import jetbrains.buildServer.messages.serviceMessages.TestSuiteStarted;
import org.jetbrains.annotations.NotNull;
import org.lilbaek.webstorm.testcafe.helpers.TestCafeUiSession;
import org.lilbaek.webstorm.testcafe.helpers.testcafe.result.TestCafeFixture;
import org.lilbaek.webstorm.testcafe.helpers.testcafe.result.TestCafeJson;
import org.lilbaek.webstorm.testcafe.helpers.testcafe.result.TestCafeTest;

public class TestCafeOutputToGeneralTestEventsConverter extends OutputToGeneralTestEventsConverter {

    @NotNull
    private OutputEventSplitter splitter;
    private ExecutionEnvironment myEnvironment;
    private TestCafeUiSession myTestCafeUiSession;

    TestCafeOutputToGeneralTestEventsConverter(@NotNull ExecutionEnvironment environment, @NotNull String testFrameworkName, @NotNull TestConsoleProperties consoleProperties, TestCafeUiSession testCafeUiSession) {
        super(testFrameworkName, consoleProperties);
        myTestCafeUiSession = testCafeUiSession;
        myEnvironment = environment;
        splitter = new OutputEventSplitter() {
            @Override
            public void onTextAvailable(@NotNull String text, @NotNull Key outputType) {
                processConsistentText(text, outputType);
            }
        };
    }

    @Override
    public void flushBufferOnProcessTermination(int exitCode) {
        super.flushBufferOnProcessTermination(exitCode);
        if(myTestCafeUiSession != null) {
            TestCafeJson testResults = myTestCafeUiSession.getTestResultFinderStrategy().findTestResults();
            if(testResults == null) {
                NotificationGroup group = NotificationGroup.balloonGroup("TestCafe Plugin - test error");
                Notification notification = group.createNotification("Test runtime terminated without providing any result", NotificationType.ERROR);
                Notifications.Bus.notify(notification, myEnvironment.getProject());
            } else {
                processAllTestResults(testResults);
            }

        }
    }

    private void processAllTestResults(TestCafeJson testResults) {
        try {
            onStartTesting();
            getProcessor().onTestsReporterAttached();
            testResults.Fixtures.forEach(this::processTestFixture);
        } finally {
            //Cleanup
            myTestCafeUiSession.getTestResultFinderStrategy().deleteTemporaryOutputFiles();
        }
    }

    private void processTestFixture(TestCafeFixture testCafeFixture) {
        GeneralTestEventsProcessor processor = getProcessor();
        TestSuiteStarted suiteStarted = new TestSuiteStarted(testCafeFixture.Name);
        //TODO: Send in location URL
        processor.onSuiteStarted(new TestSuiteStartedEvent(suiteStarted, null));
        for (TestCafeTest test : testCafeFixture.Tests) {
            processTestCase(processor, testCafeFixture, test);
        }
        processor.onSuiteFinished(new TestSuiteFinishedEvent(testCafeFixture.Name));
    }

    private void processTestCase(GeneralTestEventsProcessor processor, TestCafeFixture testCafeFixture, TestCafeTest test) {
        //TODO: Send in location URL
        processor.onTestStarted(new TestStartedEvent(test.Name, null));
        if(test.Skipped) {
            processor.onTestIgnored(new TestIgnoredEvent(test.Name, "Ignored", null));
        } else if(test.Errors.size() != 0) {
            for(String error : test.Errors) {
                processor.onTestFailure(new TestFailedEvent(
                        test.Name,
                        null,
                        error,
                        null,
                        true,
                        null,
                        null,
                        null,
                        null,
                        false,
                        false,
                        test.Duration));
            }
        }
        processor.onTestFinished(new TestFinishedEvent(test.Name, test.Duration));
    }

    @Override
    public void processConsistentText(@NotNull String text, @NotNull Key outputType) {
        super.processConsistentText(text, outputType);
    }

    @Override
    public synchronized void finishTesting() {
        super.finishTesting();
    }

    private boolean started = false;

    @Override
    protected boolean processServiceMessages(String text, Key outputType, ServiceMessageVisitor visitor) {
        if(!started) {
            this.getProcessor().onStartTesting();
            started = true;
        }
        return false;
    }
}
