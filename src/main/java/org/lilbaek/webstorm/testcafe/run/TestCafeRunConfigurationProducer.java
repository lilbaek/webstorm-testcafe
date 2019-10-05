package org.lilbaek.webstorm.testcafe.run;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;

public class TestCafeRunConfigurationProducer extends LazyRunConfigurationProducer<TestCafeRunConfiguration> {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return TestCafeRunConfigurationType.getInstance();
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
                        configuration.setGeneratedName();
                    } else {
                        //TODO: Use PsiElement to target a specific test instead of the entire fixture.
                        String content = new String(file.contentsToByteArray());
                        if (content.toLowerCase().contains("fixture")) {
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
        try {
            //Check if we are in the same context or if we should create a new configuration - we never really want to do that right now
            if(context != null && context.getLocation() != null) {
                Location location = context.getLocation();
                if(location.getVirtualFile() != null) {
                    VirtualFile file = location.getVirtualFile();
                    if(file.isDirectory()) {
                        return true;
                    } else {
                        String content = new String(file.contentsToByteArray());
                        if (content.toLowerCase().contains("fixture")) {
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
}
