package org.lilbaek.webstorm.testcafe.run;

import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.FileUrlProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestCafeTestLocator extends FileUrlProvider {
    static final TestCafeTestLocator INSTANCE = new TestCafeTestLocator();

    private TestCafeTestLocator() {
    }

    @NotNull
    @Override
    public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @Nullable String metainfo, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return super.getLocation(protocol, path, metainfo, project, scope);
    }
}
