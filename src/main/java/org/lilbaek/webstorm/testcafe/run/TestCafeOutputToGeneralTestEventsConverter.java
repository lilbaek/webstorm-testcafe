package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.OutputEventSplitter;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.openapi.util.Key;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageVisitor;
import org.jetbrains.annotations.NotNull;

public class TestCafeOutputToGeneralTestEventsConverter extends OutputToGeneralTestEventsConverter {

    @NotNull
    private OutputEventSplitter splitter;

    TestCafeOutputToGeneralTestEventsConverter(@NotNull String testFrameworkName, @NotNull TestConsoleProperties consoleProperties) {
        super(testFrameworkName, consoleProperties);

        splitter = new OutputEventSplitter() {
            @Override
            public void onTextAvailable(@NotNull String text, @NotNull Key outputType) {
                processConsistentText(text, outputType);
            }
        };
    }

    @Override
    public void processConsistentText(@NotNull String text, @NotNull Key outputType) {
        super.processConsistentText(text, outputType);
    }

    @Override
    public synchronized void finishTesting() {
        super.finishTesting();
    }

    @Override
    protected boolean processServiceMessages(String text, Key outputType, ServiceMessageVisitor visitor) {
        return false;
    }
}
