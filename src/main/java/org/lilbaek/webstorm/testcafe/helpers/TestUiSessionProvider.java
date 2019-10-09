package org.lilbaek.webstorm.testcafe.helpers;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public interface TestUiSessionProvider {

    static TestUiSessionProvider getInstance(Project project) {
        return new TestUiSessionProviderImpl(project);
    }

    @Nullable
    TestCafeUiSession getTestUiSession();
}
