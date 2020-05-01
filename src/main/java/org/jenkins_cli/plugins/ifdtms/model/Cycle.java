package org.jenkins_cli.plugins.ifdtms.model;

import java.util.List;

public class Cycle {
    private List<TestCycle> testCycle;

    public List<TestCycle> getTestCycle() {
        return testCycle;
    }

    public void setTestCycle(List<TestCycle> testCycle) {
        this.testCycle = testCycle;
    }

}
