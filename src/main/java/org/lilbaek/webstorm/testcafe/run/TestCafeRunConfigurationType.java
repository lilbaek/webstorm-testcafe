package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;
import org.jetbrains.annotations.NotNull;

public class TestCafeRunConfigurationType extends SimpleConfigurationType {

    public TestCafeRunConfigurationType() {
        super("JavaScriptTestRunnerTestCafe", "TestCafe", "TestCafe", NotNullLazyValue.createValue(() -> IconLoader.getIcon("/icons/testcafe-symbol.png")));
    }

    @NotNull
    @Override
    public String getTag() {
        return "testcafe";
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new TestCafeRunConfiguration(project, this, "TestCafe");
    }

    @NotNull
    @Override
    public RunConfigurationSingletonPolicy getSingletonPolicy() {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
    }

    @NotNull
    public static TestCafeRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(TestCafeRunConfigurationType.class);
    }
}
