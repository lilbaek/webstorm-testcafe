package org.lilbaek.webstorm.testcafe.helpers;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestUiSessionProviderImpl implements TestUiSessionProvider {
    @NotNull
    private Project myProject;

    public TestUiSessionProviderImpl(Project project) {
        myProject = project;
    }

    @Nullable
    @Override
    public TestCafeUiSession getTestUiSession() {
        File suggestedFile = createSuggestedTempOutputFile();
        //Create the additional args required to write the output file
        List<String> list = new ArrayList<>();
        list.add("-r");
        list.add("spec,json:" + tempFileLocation() + suggestedFile.getName());
        return TestCafeUiSession.create(list, new TestCafeTestFinderStrategy(suggestedFile));
    }

    /**
     * Creates a suggested temporary output file to write the data to
     */
    private File createSuggestedTempOutputFile() {

        File tempDir = new File(myProject.getBasePath() + "/" + tempFileLocation());
        String suffix = UUID.randomUUID().toString();
        String fileName = "testcafe-out-" + suffix;
        File tempFile = new File(tempDir, fileName);
        // Callers should delete this file immediately after use. Add a shutdown hook as well, in case
        // the application exits before then.
        tempFile.deleteOnExit();
        return tempFile;
    }

    @NotNull
    private String tempFileLocation() {
        return "node_modules/testcafe/tmp/";
    }
}
