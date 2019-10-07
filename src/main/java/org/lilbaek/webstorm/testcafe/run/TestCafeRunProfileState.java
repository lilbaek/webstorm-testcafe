package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
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
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

public class TestCafeRunProfileState extends CommandLineState {

    private ExecutionEnvironment environment;
    private final TestCafeRunConfiguration configuration;
    private final TestCafeExecutionType myExecutionType;

    TestCafeRunProfileState(ExecutionEnvironment environment, TestCafeRunConfiguration configuration) {
        super(environment);
        this.environment = environment;
        this.configuration = configuration;
        myExecutionType = findExecutionType(environment.getExecutor());
    }

    private TestCafeExecutionType findExecutionType(Executor executor) {
        if (executor.equals(DefaultDebugExecutor.getDebugExecutorInstance())) {
            return TestCafeExecutionType.DEBUG;
        }
        return TestCafeExecutionType.RUN;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();
        Project project = getEnvironment().getProject();
        String basePath = project.getBasePath();

        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath("node");

        @SystemIndependent
        String testFolder = getTestFolder();

        commandLine.withWorkDirectory(basePath);
        commandLine.withRedirectErrorStream(true);
        commandLine.addParameter("node_modules/testcafe/lib/cli/index.js");
        if(configuration.options.browser != null) {
            commandLine.addParameter(configuration.options.browser);
        }
        commandLine.addParameter(testFolder);
        if(TestCafeCurrentSetup.TestName != null && !TestCafeCurrentSetup.TestName.isEmpty()) {
            commandLine.addParameter("-t");
            String testName = removeIllegalChars(TestCafeCurrentSetup.TestName);
            commandLine.addParameter(testName);
        }
        if(TestCafeCurrentSetup.FixtureName != null && !TestCafeCurrentSetup.FixtureName.isEmpty()) {
            commandLine.addParameter("-f");
            String fixtureName = removeIllegalChars(TestCafeCurrentSetup.FixtureName);
            commandLine.addParameter(fixtureName);
        }
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);
        return new ColoredProcessHandler(commandLine);
    }

    private String removeIllegalChars(String str) {
        return str.replace("'", "").replace("\"", "").replace("`", "");
    }

    @SystemIndependent
    private String getTestFolder() {
        return TestCafeCurrentSetup.Folder == null || TestCafeCurrentSetup.Folder.isEmpty()
                ? this.getEnvironment().getProject().getBasePath()
                : TestCafeCurrentSetup.Folder;
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
