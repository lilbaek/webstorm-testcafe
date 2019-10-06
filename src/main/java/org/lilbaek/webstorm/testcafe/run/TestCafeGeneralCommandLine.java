package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.CommandLineUtil;
import com.intellij.execution.Platform;
import com.intellij.execution.configurations.GeneralCommandLine;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TestCafeGeneralCommandLine extends GeneralCommandLine {

    @NotNull
    @Override
    protected List<String> prepareCommandLine(@NotNull String command, @NotNull List<String> parameters, @NotNull Platform platform) {
        return super.prepareCommandLine(command, parameters, platform);
        //Don't do all kinds of Windows magic woodo. Just accept what is send in
        /*List<String> commandLine = new ArrayList<>(parameters.size() + 1);
        commandLine.addAll(parameters);
        return commandLine;*/
    }
}
