package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
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

public class TestCafeRunConfiguration extends LocatableConfigurationBase<TestCafeRunProfileState> {

    Options options = new Options();

    TestCafeRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
        options.browser = "chrome";
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new TestCafeSettingsEditor(getProject());
    }

    @Override
    public void checkConfiguration() {
    }

    @Nullable
    @Override
    public TestCafeRunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        return new TestCafeRunProfileState(executionEnvironment, this);
    }

    static class Options {
        String testCafeFolder;
        String testCafeTestName;
        String browser;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icons/testcafe-symbol.png");
    }


    static void writeOptions(Options options, Element element) {
        Element e = new Element(TestCafeRunConfiguration.class.getSimpleName());
        if (options.testCafeFolder != null) {
            e.setAttribute("testcafe-folder", options.testCafeFolder);
        }
        if (options.testCafeTestName != null) {
            e.setAttribute("testcafe-testname", options.testCafeTestName);
        }
        if(options.browser != null) {
            e.setAttribute("testcafe-browser", options.browser);
        }
        element.addContent(e);
    }

    static Options readOptions(Element element) {
        Options result = new Options();

        String name = TestCafeRunConfiguration.class.getSimpleName();
        Element optionsElement = element.getChild(name);

        if (optionsElement != null) {
            Attribute folder = optionsElement.getAttribute("testcafe-folder");
            Attribute browser = optionsElement.getAttribute("testcafe-browser");
            Attribute testName = optionsElement.getAttribute("testcafe-testname");
            result.testCafeFolder = null;
            result.browser = null;
            result.testCafeTestName = null;
            if (folder != null) {
                result.testCafeFolder = folder.getValue();
            }
            if(browser != null) {
                result.browser = browser.getValue();
            }
            if(testName != null) {
                result.testCafeTestName = testName.getValue();
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
        if (options.testCafeFolder == null) {
            return null;
        }
        return "TestCafe";
    }
}
