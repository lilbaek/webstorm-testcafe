package org.lilbaek.webstorm.testcafe.debug

import com.intellij.execution.ExecutionException
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.then

fun prepareTestCafeDebugger(project: Project, debuggableWebBrowser: DebuggableWebBrowser, producer: ThrowableComputable<RunContentDescriptor, ExecutionException>): Promise<RunContentDescriptor> {
    return debuggableWebBrowser.debugEngine.prepareDebugger(project, debuggableWebBrowser.webBrowser)
            .then { it -> producer.compute() }
}