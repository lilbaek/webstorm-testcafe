package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.autotest.AbstractAutoTestManager;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

import java.util.List;

public class TestCafeRunProfileState extends CommandLineState {

    private final TestCafeRunConfiguration configuration;

    TestCafeRunProfileState(ExecutionEnvironment environment, TestCafeRunConfiguration configuration) {
        super(environment);
        this.configuration = configuration;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();
        Project project = getEnvironment().getProject();
        String basePath = project.getBasePath();

        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath("node.exe");

        @SystemIndependent
        String testFolder = getTestFolder();

        commandLine.withWorkDirectory(basePath);
        commandLine.withRedirectErrorStream(true);
        commandLine.addParameter("node_modules/testcafe/lib/cli/index.js");
        commandLine.addParameter("chrome");
        commandLine.addParameter(testFolder);
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);


        return new ColoredProcessHandler(commandLine);
    }

    @SystemIndependent
    private String getTestFolder() {
        return configuration.options.testCafeFolder == null || configuration.options.testCafeFolder.isEmpty()
                ? this.getEnvironment().getProject().getBasePath()
                : configuration.options.testCafeFolder;
    }

    @Nullable
    @Override
    protected ConsoleView createConsole(@NotNull Executor executor) {
        RunConfiguration runConfiguration = (RunConfiguration) this.getEnvironment().getRunProfile();
        SMTRunnerConsoleProperties properties = new ConsoleProperties(runConfiguration, executor);
        SMTRunnerConsoleView consoleView = new SMTRunnerConsoleView(properties);
        SMTestRunnerConnectionUtil.initConsoleView(consoleView, properties.getTestFrameworkName());
        return consoleView;
    }

    private static class ConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {

        ConsoleProperties(@NotNull RunConfiguration config, @NotNull Executor executor) {
            super(config, "TestCafe", executor);

            setIfUndefined(TestConsoleProperties.TRACK_RUNNING_TEST, true);
            setIfUndefined(TestConsoleProperties.OPEN_FAILURE_LINE, true);
            setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);
            setIfUndefined(TestConsoleProperties.SHOW_STATISTICS, true);
            setIfUndefined(TestConsoleProperties.SELECT_FIRST_DEFECT, true);
            setIfUndefined(TestConsoleProperties.SCROLL_TO_SOURCE, true);
        }

        @Nullable
        @Override
        public SMTestLocator getTestLocator() {
            return TestCafeTestLocator.INSTANCE;
        }

        @Override
        public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String testFrameworkName, @NotNull TestConsoleProperties consoleProperties) {
            return new OutputToGeneralTestEventsConverter(testFrameworkName, consoleProperties) {

                @Override
                public synchronized void finishTesting() {
                    super.finishTesting();
                }

                @Override
                protected boolean processServiceMessages(String text, Key outputType, ServiceMessageVisitor visitor) {
                    return false;
                }
            };
        }
    }
}
