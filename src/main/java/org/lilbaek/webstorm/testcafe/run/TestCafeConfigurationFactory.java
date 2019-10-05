package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TestCafeConfigurationFactory extends ConfigurationFactory {

    private static final String FACTORY_NAME = "TESTCAFE_FACTORY";

    protected TestCafeConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new TestCafeRunConfiguration(project, this, "TestCafe");
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    @Nullable
    @Override
    public Icon getIcon()  {
        return IconLoader.getIcon("/icons/testcafe-symbol.png");
    }

}