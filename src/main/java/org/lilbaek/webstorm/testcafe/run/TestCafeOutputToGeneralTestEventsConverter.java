package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
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

    private ExecutionEnvironment myEnvironment;
    private TestCafeUiSession myTestCafeUiSession;

    TestCafeOutputToGeneralTestEventsConverter(@NotNull ExecutionEnvironment environment, @NotNull String testFrameworkName, @NotNull TestConsoleProperties consoleProperties, TestCafeUiSession testCafeUiSession) {
        super(testFrameworkName, consoleProperties);
        myTestCafeUiSession = testCafeUiSession;
        myEnvironment = environment;
    }

    @Override
    public void flushBufferOnProcessTermination(int exitCode) {
        super.flushBufferOnProcessTermination(exitCode);
        if(myTestCafeUiSession != null) {
            TestCafeJson testResults = myTestCafeUiSession.getTestResultFinderStrategy().findTestResults();
            if(testResults == null) {
                NotificationGroup group = NotificationGroup.balloonGroup("TestCafe Plugin - test error");
                Notification notification = group.createNotification("TestCafe terminated without providing any result", NotificationType.ERROR);
                Notifications.Bus.notify(notification, myEnvironment.getProject());
            } else {
                processAllTestResults(testResults);
            }
        }
    }

    private void processAllTestResults(TestCafeJson testResults) {

            onStartTesting();
            getProcessor().onTestsReporterAttached();
            testResults.Fixtures.forEach(this::processTestFixture);

    }

    private void processTestFixture(TestCafeFixture testCafeFixture) {
        GeneralTestEventsProcessor processor = getProcessor();
        TestSuiteStarted suiteStarted = new TestSuiteStarted(testCafeFixture.Name);
        processor.onSuiteStarted(new TestSuiteStartedEvent(suiteStarted, testCafeFixture.Path));
        for (TestCafeTest test : testCafeFixture.Tests) {
            processTestCase(processor, testCafeFixture, test);
        }
        processor.onSuiteFinished(new TestSuiteFinishedEvent(testCafeFixture.Name));
    }

    private void processTestCase(GeneralTestEventsProcessor processor, TestCafeFixture testCafeFixture, TestCafeTest test) {
        processor.onTestStarted(new TestStartedEvent(test.Name, testCafeFixture.Path));
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
