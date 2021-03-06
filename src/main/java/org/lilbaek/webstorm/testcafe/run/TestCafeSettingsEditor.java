// Copyright 2000-2017 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.lilbaek.webstorm.testcafe.run;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;

public class TestCafeSettingsEditor extends SettingsEditor<TestCafeRunConfiguration> {

    private JPanel myPanel;
    private ComboBox<String> browserChooser;
    private JCheckBox checkBoxLiveMode;
    private JCheckBox checkBoxHeadlessMode;
    private TextFieldWithBrowseButton nodeModulesLocation;
    private TextFieldWithBrowseButton cwd;
    private JTextField customArgs;

    TestCafeSettingsEditor(Project project) {
        browserChooser.addItem("chrome");
        browserChooser.addItem("firefox");
        browserChooser.addItem("opera");
        browserChooser.addItem("safari");
        browserChooser.addItem("edge");
        nodeModulesLocation.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(false, true, false, false, false, false)));
        cwd.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(false, true, false, false, false, false)));
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    @Override
    protected void resetEditorFrom(@NotNull TestCafeRunConfiguration configuration) {
        browserChooser.setSelectedItem(configuration.options.browser);
        checkBoxLiveMode.setSelected(configuration.options.liveMode);
        checkBoxHeadlessMode.setSelected(configuration.options.headlessMode);
        customArgs.setText(configuration.options.customArgs);
        nodeModulesLocation.setText(configuration.options.nodeModulesLocation);
        cwd.setText(configuration.options.cwd);
    }

    @Override
    protected void applyEditorTo(@NotNull TestCafeRunConfiguration configuration) {
        String browser = (String) browserChooser.getSelectedItem();
        if(browser != null) {
            configuration.options.browser = browser;
        }
        configuration.options.liveMode = checkBoxLiveMode.isSelected();
        configuration.options.headlessMode = checkBoxHeadlessMode.isSelected();
        configuration.options.customArgs = customArgs.getText();
        configuration.options.nodeModulesLocation = nodeModulesLocation.getText();
        configuration.options.cwd = cwd.getText();
    }
}
