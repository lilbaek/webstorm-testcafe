package org.lilbaek.webstorm.testcafe.run;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;

public class TestCafeRunConfigurationProducer extends LazyRunConfigurationProducer<TestCafeRunConfiguration> {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return TestCafeRunConfigurationType.getInstance();
    }

    @Override
    public boolean shouldReplace(@NotNull ConfigurationFromContext self, @NotNull ConfigurationFromContext other) {
        return true;
    }

    @Override
    protected boolean setupConfigurationFromContext(TestCafeRunConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement) {
        try {
            if(context != null && context.getLocation() != null) {
                Location location = context.getLocation();
                if(location.getVirtualFile() != null) {
                    VirtualFile file = location.getVirtualFile();
                    if(file.isDirectory()) {
                        configuration.options.testCafeFolder = file.getPath();
                        configuration.options.testCafeTestName = null;
                        configuration.setGeneratedName();
                    } else {
                        //TODO: Use PsiElement to target a specific test instead of the entire fixture.
                        String content = new String(file.contentsToByteArray());
                        if (content.toLowerCase().contains("fixture")) {
                            PsiElement element = sourceElement.get();
                            if(element.getNode() != null) {
                                IElementType type = element.getNode().getElementType();
                                String typeAsString = type.toString();
                                if (typeAsString.equals("JS:STRING_LITERAL")) {
                                    configuration.options.testCafeTestName = element.getText().replace("'", "").replace("\"", "");
                                } else {
                                    configuration.options.testCafeTestName = null;
                                }
                            } else {
                                configuration.options.testCafeTestName = null;
                            }
                            configuration.options.testCafeFolder = file.getPath();
                            configuration.setGeneratedName();
                            return true;

                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(TestCafeRunConfiguration configuration, ConfigurationContext context) {
        return true;
    }
}
