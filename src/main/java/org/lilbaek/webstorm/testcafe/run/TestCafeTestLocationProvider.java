package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class TestCafeTestLocationProvider implements SMTestLocator {
    public TestCafeTestLocationProvider(File workingDir) {

    }

    @NotNull
    @Override
    public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return Collections.emptyList();
    }
}
