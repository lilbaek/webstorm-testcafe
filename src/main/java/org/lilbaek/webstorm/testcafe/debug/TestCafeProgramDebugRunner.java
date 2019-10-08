package org.lilbaek.webstorm.testcafe.debug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentDescriptorReusePolicy;
import com.intellij.javascript.debugger.DebuggableFileFinder;
import com.intellij.javascript.debugger.DebuggableFileFinderImpl;
import com.intellij.javascript.debugger.LocalFileSystemFileFinder;
import com.intellij.javascript.debugger.RemoteDebuggingFileFinder;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.util.net.NetUtils;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.nodeJs.NodeDebugProgramRunnerKt;
import com.jetbrains.nodeJs.NodeJSFileFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lilbaek.webstorm.testcafe.run.TestCafeRunConfiguration;
import org.lilbaek.webstorm.testcafe.run.TestCafeRunProfileState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class TestCafeProgramDebugRunner extends GenericProgramRunner {

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(RunProfileState state, ExecutionEnvironment environment) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();
        final InetSocketAddress debugAddress = this.getDebugAddress(state);

        TestCafeRunProfileState executor =  (TestCafeRunProfileState)state;
        ExecutionResult result = executor.execute(debugAddress.getPort());
        try {
            return withReusePolicy(runWithDebuggerConnector(executor, result, environment, debugAddress));
        } catch (IOException e) {
            return null;
        }
    }

    @NotNull
    private static RunContentDescriptor withReusePolicy(@NotNull RunContentDescriptor descriptor) {
        descriptor.setReusePolicy(new RunContentDescriptorReusePolicy() {
            @Override
            public boolean canBeReusedBy(@NotNull RunContentDescriptor newDescriptor) {
                return true;
            }
        });
        return descriptor;
    }

    private RunContentDescriptor runWithDebuggerConnector(final TestCafeRunProfileState state, ExecutionResult result, final ExecutionEnvironment environment, InetSocketAddress socketAddress) throws IOException, ExecutionException {
        Project project = environment.getProject();
        NodeJSFileFinder fileFinder = new NodeJSFileFinder(project);
        XDebugSession session = XDebuggerManager.getInstance(project).startSession(
                environment,
                new XDebugProcessStarter() {
                    @Override
                    @NotNull
                    public XDebugProcess start(@NotNull XDebugSession session) {
                        XDebugProcess process = NodeDebugProgramRunnerKt.createNodeJsDebugProcess(socketAddress, session, result, fileFinder);
                        return process;
                    }
                }
        );
        return session.getRunContentDescriptor();
    }

    @NotNull
    @Override
    public String getRunnerId() {
        return "TESTCAFE_WEBSTORM_PROGRAM_DEBUG_RUNNER";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof TestCafeRunConfiguration;
    }


    private InetSocketAddress getDebugAddress(final RunProfileState state) throws ExecutionException {
        return computeDebugAddress();
    }

    @NotNull
    private InetSocketAddress computeDebugAddress() throws ExecutionException {
        try {
            return new InetSocketAddress(InetAddress.getLoopbackAddress(), NetUtils.findAvailableSocketPort());
        }
        catch (IOException e) {
            throw new ExecutionException("Cannot find available port", e);
        }
    }
}