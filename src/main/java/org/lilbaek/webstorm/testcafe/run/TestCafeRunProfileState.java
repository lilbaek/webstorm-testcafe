package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.javascript.nodejs.NodeCommandLineUtil;
import com.intellij.javascript.nodejs.debug.NodeLocalDebugRunProfileState;
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.util.Collections;

public class TestCafeRunProfileState implements RunProfileState, NodeLocalDebugRunProfileState {

    private ExecutionEnvironment environment;
    private final TestCafeRunConfiguration configuration;
    private final TestCafeExecutionType myExecutionType;
    private TextConsoleBuilder myConsoleBuilder;

    TestCafeRunProfileState(ExecutionEnvironment environment, TestCafeRunConfiguration configuration) {
        super();
        this.environment = environment;
        this.configuration = configuration;
        myExecutionType = findExecutionType(environment.getExecutor());
        final Project project = environment.getProject();
        final GlobalSearchScope searchScope = GlobalSearchScopes.executionScope(project, environment.getRunProfile());
        myConsoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project, searchScope);
    }

    private TestCafeExecutionType findExecutionType(Executor executor) {
        if (executor.equals(DefaultDebugExecutor.getDebugExecutorInstance())) {
            return TestCafeExecutionType.DEBUG;
        }
        return TestCafeExecutionType.RUN;
    }

    @NotNull
    @Override
    public ExecutionResult execute(int debugPort) throws ExecutionException {
        NodeJsInterpreter interpreter = NodeJsInterpreterRef.createProjectRef().resolveNotNull(environment.getProject());
        GeneralCommandLine commandLine = this.createCommandLine(interpreter, debugPort);
        NodeCommandLineConfigurator.find(interpreter).configure(commandLine);
        OSProcessHandler processHandler = NodeCommandLineUtil.createProcessHandler(commandLine, false);

        final ConsoleView console = myConsoleBuilder.getConsole();
        console.attachToProcess(processHandler);

        return new DefaultExecutionResult(console, processHandler);
    }


    @NotNull
    private GeneralCommandLine createCommandLine(NodeJsInterpreter interpreter, int debugPort) throws ExecutionException {
        GeneralCommandLine commandLine = NodeCommandLineUtil.createCommandLine(SystemInfo.isWindows ? false : null);
        Project project = environment.getProject();
        String basePath = project.getBasePath();
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
        if(myExecutionType == TestCafeExecutionType.DEBUG) {
            NodeCommandLineUtil.addNodeOptionsForDebugging(commandLine, Collections.emptyList(), debugPort, true, interpreter, true);
        }
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);
        return commandLine;
    }

    private String removeIllegalChars(String str) {
        return str.replace("'", "").replace("\"", "").replace("`", "");
    }

    @SystemIndependent
    private String getTestFolder() {
        return TestCafeCurrentSetup.Folder == null || TestCafeCurrentSetup.Folder.isEmpty()
                ? this.environment.getProject().getBasePath()
                : TestCafeCurrentSetup.Folder;
    }

    /*
    @Nullable
    @Override
    protected ConsoleView createConsole(@NotNull Executor executor) {
        RunConfiguration runConfiguration = (RunConfiguration) this.environment.getRunProfile();
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
    }*/
}
