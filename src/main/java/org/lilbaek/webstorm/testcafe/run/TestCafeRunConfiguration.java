package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.javascript.nodejs.debug.NodeDebugRunConfiguration;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TestCafeRunConfiguration extends LocatableConfigurationBase<TestCafeRunProfileState> implements NodeDebugRunConfiguration {

    Options options = new Options();
    private NodeJsInterpreterRef myInterpreterRef;

    TestCafeRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
        options.browser = "chrome";
        this.myInterpreterRef = NodeJsInterpreterRef.createProjectRef();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new TestCafeSettingsEditor(getProject());
    }

    @Override
    public void checkConfiguration() {
    }

    public boolean isLiveMode() {
        return TestCafeCurrentSetup.TestName != null && !TestCafeCurrentSetup.TestName.isEmpty() && options.liveMode;
    }

    @Nullable
    @Override
    public TestCafeRunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        return new TestCafeRunProfileState(executionEnvironment, this);
    }

    static class Options {
        boolean liveMode;
        String browser;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icons/testcafe-symbol.png");
    }


    @NotNull
    public NodeJsInterpreterRef getInterpreterRef() {
        final NodeJsInterpreterRef myInterpreterRef = this.myInterpreterRef;
        return myInterpreterRef;
    }

    @NotNull
    public void setInterpreterRef(@NotNull final NodeJsInterpreterRef interpreterRef) {
        this.myInterpreterRef = interpreterRef;
    }

    static void writeOptions(Options options, Element element) {
        Element e = new Element(TestCafeRunConfiguration.class.getSimpleName());
        if(options.browser != null) {
            e.setAttribute("testcafe-browser", options.browser);
        }
        e.setAttribute("testcafe-live-mode", String.valueOf(options.liveMode));
        element.addContent(e);
    }

    static Options readOptions(Element element) {
        Options result = new Options();

        String name = TestCafeRunConfiguration.class.getSimpleName();
        Element optionsElement = element.getChild(name);

        if (optionsElement != null) {
            Attribute browser = optionsElement.getAttribute("testcafe-browser");
            Attribute liveMode = optionsElement.getAttribute("testcafe-live-mode");
            result.browser = null;
            if(browser != null) {
                result.browser = browser.getValue();
            }
            if(liveMode != null) {
                result.liveMode = Boolean.parseBoolean(liveMode.getValue());
            }
        }
        return result;
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        this.options = readOptions(element);
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        writeOptions(this.options, element);
    }

    @Nullable
    @Override
    public String suggestedName() {
        return "TestCafe";
    }
}
