package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.javascript.nodejs.NodeCommandLineUtil;
import com.intellij.javascript.nodejs.NodeConsoleAdditionalFilter;
import com.intellij.javascript.nodejs.NodeStackTraceFilter;
import com.intellij.javascript.nodejs.debug.NodeLocalDebugRunProfileState;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.lang.javascript.buildTools.TypeScriptErrorConsoleFilter;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lilbaek.webstorm.testcafe.helpers.TestCafeUiSession;
import org.lilbaek.webstorm.testcafe.helpers.TestUiSessionProvider;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class TestCafeRunProfileState implements RunProfileState, NodeLocalDebugRunProfileState {

    private ExecutionEnvironment myEnvironment;
    private final TestCafeRunConfiguration myConfiguration;
    private TestCafeUiSession myTestCafeUiSession;

    TestCafeRunProfileState(ExecutionEnvironment environment, TestCafeRunConfiguration configuration) {
        super();
        myEnvironment = environment;
        myConfiguration = configuration;
    }

    @NotNull
    @Override
    public ExecutionResult execute(int debugPort) throws ExecutionException {
        Project project = this.myEnvironment.getProject();
        final NodeJsInterpreter interpreter = this.myConfiguration.getInterpreterRef().resolve(project);
        final NodeJsLocalInterpreter localInterpreter = NodeJsLocalInterpreter.castAndValidate(interpreter);
        final ProcessHandler processHandler = this.startProcess(localInterpreter, debugPort);
        ProcessTerminatedListener.attach(processHandler);

        String basePath = project.getBasePath();

        final File workingDir = new File(basePath);
        final ConsoleView consoleView;
        if (isLiveMode()) {
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            processHandler.startNotify();
            consoleView.attachToProcess(processHandler);
        } else {
            consoleView = createTestConsoleView(processHandler, this.myEnvironment, new TestCafeTestLocationProvider(workingDir));
        }
        consoleView.addMessageFilter(new NodeStackTraceFilter(project, workingDir));
        consoleView.addMessageFilter(new NodeConsoleAdditionalFilter(project, workingDir));
        consoleView.addMessageFilter(new TypeScriptErrorConsoleFilter(project, workingDir));

        final DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
        executionResult.setRestartActions(new ToggleAutoTestAction());
        return executionResult;
    }

    private ConsoleView createTestConsoleView(@NotNull ProcessHandler processHandler, @NotNull ExecutionEnvironment env, @NotNull SMTestLocator locator) {
        RunConfiguration runConfiguration = (RunConfiguration) env.getRunProfile();
        SMTRunnerConsoleProperties consoleProperties = new ConsoleProperties(env, runConfiguration, env.getExecutor(), locator, myTestCafeUiSession);
        final ConsoleView testsOutputConsoleView = SMTestRunnerConnectionUtil.createConsole(consoleProperties);
        testsOutputConsoleView.attachToProcess(processHandler);
        Disposer.register(env.getProject(), testsOutputConsoleView);

        return testsOutputConsoleView;
    }

    @NotNull
    private ProcessHandler startProcess(@NotNull NodeJsLocalInterpreter interpreter, int debugPort) throws ExecutionException {
        Project project = myEnvironment.getProject();
        final GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.withCharset(StandardCharsets.UTF_8);
        String basePath = project.getBasePath();
        commandLine.withWorkDirectory(basePath);
        commandLine.setExePath(interpreter.getInterpreterSystemDependentPath());
        NodeCommandLineUtil.addNodeOptionsForDebugging(commandLine, Collections.emptyList(), debugPort, true, interpreter, true);

        commandLine.withRedirectErrorStream(true);
        commandLine.addParameter("node_modules/testcafe/lib/cli/index.js");
        if(myConfiguration.options.browser != null) {
            commandLine.addParameter(myConfiguration.options.browser);
        }
        if(TestCafeCurrentSetup.Folder == null || TestCafeCurrentSetup.Folder.isEmpty()) {
            return handleBadConfiguration("Please only start a run using the context menu. Running using the toolbar is not supported.");
        }
        commandLine.addParameter(TestCafeCurrentSetup.Folder);
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
        if (isLiveMode()) {
            commandLine.addParameter("-L");
        } else {
            myTestCafeUiSession = TestUiSessionProvider.getInstance(project).getTestUiSession();
            if (myTestCafeUiSession != null) {
                for (String arg : myTestCafeUiSession.getCommandLineArguments()) {
                    commandLine.addParameter(arg);
                }
            }
        }
        //Make sure we get colors in the console from nodeJs
        commandLine.addParameter("--color");
        return NodeCommandLineUtil.createProcessHandler(commandLine, false);
    }

    private boolean isLiveMode() {
        return TestCafeCurrentSetup.TestName != null && !TestCafeCurrentSetup.TestName.isEmpty() && myConfiguration.options.liveMode;
    }

    private ProcessHandler handleBadConfiguration(String errorMessage) throws ExecutionException {
        NotificationGroup group = NotificationGroup.balloonGroup("TestCafe Plugin");
        Notification notification = group.createNotification(errorMessage, NotificationType.ERROR);
        Notifications.Bus.notify(notification, myEnvironment.getProject());
        throw new ExecutionException(errorMessage);
    }

    private String removeIllegalChars(String str) {
        return str.replace("'", "").replace("\"", "").replace("`", "");
    }

    private static class ConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {
        private final SMTestLocator myLocator;
        private TestCafeUiSession myTestCafeUiSession;
        private ExecutionEnvironment myEnvironment;
        ConsoleProperties(@NotNull ExecutionEnvironment environment, @NotNull RunConfiguration config, @NotNull Executor executor, @NotNull SMTestLocator locator, TestCafeUiSession testCafeUiSession) {
            super(config, "TestCafe", executor);
            setIfUndefined(TestConsoleProperties.TRACK_RUNNING_TEST, false);
            setIfUndefined(TestConsoleProperties.OPEN_FAILURE_LINE, false);
            setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);
            setIfUndefined(TestConsoleProperties.SHOW_STATISTICS, false);
            setIfUndefined(TestConsoleProperties.SELECT_FIRST_DEFECT, false);
            setIfUndefined(TestConsoleProperties.SCROLL_TO_SOURCE, false);
            myLocator = locator;
            myTestCafeUiSession = testCafeUiSession;
            myEnvironment = environment;}

        @Nullable
        @Override
        public SMTestLocator getTestLocator() {
            return myLocator;
        }


        @Override
        public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String testFrameworkName, @NotNull TestConsoleProperties consoleProperties) {
            return new TestCafeOutputToGeneralTestEventsConverter(myEnvironment, testFrameworkName, consoleProperties, myTestCafeUiSession);
        }
    }
}
