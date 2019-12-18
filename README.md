
# iTMS JUnit Report Publisher

Allow user publishes test results to iTMS for JUnit test cases.

![](docs/images/infodation-vietnam-logo.jpg)

## Adding iTMS Servers in Jenkins global settings

After installation configure Jenkins global settings to establish
connection with iTMS Server(s). Follow the below steps:

-   Launch Jenkins and access via a web browser.
-   Click on "Manage Jenkins" from the Menu as illustrated in the below
    screenshot.
![](docs/images/manage-Jenkins.png)

-   Click on "Configure System".
![](docs/images/configure-system.png)

- Locate the section "iTMS JUnit Server Configuration". Then fill up infomation to establish connection to iTMS server. Note: Login iTMS to get the Token.
- Click on "Test Configuration" to validate connection to iTMS server.
- Click "Save" when success to connect iTMS server.
![](docs/images/connect-iTMS.png)

# **Configuring a Jenkins job**
1\. Click the \<job name\> on the Jenkins home page.
![](docs/images/select-project.png)

2\. Click on "Configure" action.
![](docs/images/configure-project.png)

3\. Add a post-build action.
![](docs/images/add-post-build.png)

4\. Select "Publish JUnit test result to iTMS"
![](docs/images/seclect-publish-junit.png)

5\. Correct the configuration to publish junit report to iTMS server. Then click on "Test Configuration" to validate the provided information. After all, "Save" it.
![](docs/images/configure-post-build.png)

6\. Click on "Build now" action.
![](docs/images/build-now.png)

# **License**
Copyright (c) 2019 iTMS

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.