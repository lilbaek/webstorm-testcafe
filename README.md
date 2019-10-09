# TestCafe runner for Webstorm

Integration with [TestCafe](https://devexpress.github.io/testcafe/) TestCafe A node.js tool to automate end-to-end web testing. 
This plugin allows you to run TestCafe tests directly from Webstorm.

* Run a specific test, fixture, all tests in a file or directory via the context menu
* Debug a specific test, fixture, all tests in a file or directory via the context menu
* View test results in the run window

## Requirements

TestCafe should be installed in your project as a local package. To install it, use the npm install testcafe command or add TestCafe to dependencies in your package.json file. Your project should contain TestCafe modules in node_modules\testcafe\.... 

### Change browser

You can change the browser in the run configuration.

![Configuration](./images/runconfiguration.png)

### Running/debugging a specific test

To run/debug a specific test, invoke the context menu when the cursor is placed on the test name.

![Specific](./images/onetest.png)

### Running/debugging all tests in a fixture

To run/debug all tests in a test fixture, invoke the context menu when the cursor is placed on the fixture name.

![Fixture](./images/onefixture.png)

### Running all/debugging tests in a file

To run/debug all tests in the current file, invoke the context menu for this file.

### Running all tests in a folder

To run all test files in a folder, invoke the context menu for this folder.

### Tests results

The test results are shown in the run window

![All](./images/results.png)