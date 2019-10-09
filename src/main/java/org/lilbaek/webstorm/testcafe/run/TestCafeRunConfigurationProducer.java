package org.lilbaek.webstorm.testcafe.run;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionExpression;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
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
                    TestCafeCurrentSetup.TestName = null;
                    TestCafeCurrentSetup.FixtureName = null;
                    TestCafeCurrentSetup.Folder = null;
                    if(file.isDirectory()) {
                        TestCafeCurrentSetup.Folder = file.getPath();
                        configuration.setGeneratedName();
                    } else {
                        return CheckIfFileIsTestCafe(configuration, sourceElement, file);
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private boolean CheckIfFileIsTestCafe(TestCafeRunConfiguration configuration, Ref<PsiElement> sourceElement, VirtualFile file) throws IOException {
        //File should contain a fixture or we are not interested
        String content = new String(file.contentsToByteArray());
        if (content.toLowerCase().contains("fixture")) {
            PsiElement element = sourceElement.get();
            if(element.getNode() != null) {
                IElementType type = element.getNode().getElementType();
                String typeAsString = type.toString();

                //Lets check if we can find a test by searching up in our tree
                    /*
                    JSCallExpression
                        JSReferenceExpression
                            JSIdentifier (Test identifier)
                        JSAugmentList
                            xxx
                            xxx
                                JStringLiteral (Test Name)
                        FunctionExpression ->
                            Test code
                     */
                PsiElement parentTestElement = PsiTreeUtil.findFirstParent(element, psiElement -> psiElement.getText().toLowerCase().startsWith("test"));
                if(parentTestElement != null) {
                    //Lets find first child of type STRING_LITERAL that should give use the name of the test
                    PsiElement textElement = PsiTreeUtil.findChildOfType(parentTestElement, JSLiteralExpression.class, true, TypeScriptFunctionExpression.class);
                    if(textElement != null) {
                        TestCafeCurrentSetup.TestName = removeIllegalChars(textElement.getText());
                    }
                } else if (typeAsString.equals("JS:STRING_LITERAL")) {
                    //Check if we are targeting a text string that we can use for testing. Might give false positive but it is better than nothing
                    PsiElement testElement = PsiTreeUtil.findFirstParent(element, psiElement -> psiElement.getText().toLowerCase().startsWith("test"));
                    if(testElement != null) {
                        TestCafeCurrentSetup.TestName = removeIllegalChars(element.getText());
                    }
                }
                else if (typeAsString.equals("JS:STRING_TEMPLATE_PART")) {
                    //Check if we can get a fixture name instead. Might give false positive but it is better than nothing.
                    PsiElement fixtureElement = PsiTreeUtil.findFirstParent(element, psiElement -> psiElement.getText().toLowerCase().startsWith("fixture"));
                    if(fixtureElement != null) {
                        TestCafeCurrentSetup.FixtureName = removeIllegalChars(element.getText());
                    }
                }
            }
            TestCafeCurrentSetup.Folder = file.getPath();
            configuration.setGeneratedName();
            return true;
        }
        return false;
    }

    private String removeIllegalChars(String str) {
        return str.replace("'", "").replace("\"", "").replace("`", "");
    }

    @Override
    public boolean isConfigurationFromContext(TestCafeRunConfiguration configuration, ConfigurationContext context) {
        //TODO: Change the method to actually look at the config + context. This way we could have one config pr. type - fixture, tests file/folder.
        return true;
    }
}
