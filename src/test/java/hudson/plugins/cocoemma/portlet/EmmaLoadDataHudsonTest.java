package hudson.plugins.cocoemma.portlet;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.plugins.cocoemma.CocoEmmaPublisher;
import hudson.plugins.cocoemma.portlet.bean.EmmaCoverageResultSummary;
import hudson.tasks.Builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static junit.framework.TestCase.assertEquals;
import org.joda.time.LocalDate;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Tests {@link hudson.plugins.cocoemma.portlet.EmmaLoadData} in a Hudson environment.
 *
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 * @author Mauro Durante Junior (Mauro.Durantejunior@sonyericsson.com)
 */
public class EmmaLoadDataHudsonTest extends JenkinsRule {

    /**
     * This method tests loadChartDataWithinRange() when it has positive number of days.
     * Tests {@link hudson.plugins.cocoemma.portlet.EmmaLoadData#loadChartDataWithinRange(java.util.List, int)}.
     *
     * @throws Exception if so.
     */
   public void testLoadChartDataWithinRangePositiveNumberOfDays() throws Exception {

        final float expectedBlockCoverage = 0.5f;
        final float expectedClassCoverage = 13.7f;
        final float expectedLineCoverage = 0.6f;
        final float expectedMethodCoverage = 2.0f;
        final int numberOfDays = 1;
        final int summaryMapSize = 1;

        //Create the project
        FreeStyleProject job1 = createFreeStyleProject("job1");

        //Make it do something, in this case it writes a coverage report to the workspace.
        job1.getBuildersList().add(
          new CopyResourceToWorkspaceBuilder(getClass().getResourceAsStream("/hudson/plugins/cocoemma/coveragePortlet.xml"),
                        "reports/coverage/coveragePortlet.xml"));
        //Add a cocoemma publisher
        CocoEmmaPublisher cocoEmmaPublisher = new CocoEmmaPublisher();
        cocoEmmaPublisher.includes = "reports/coverage/coveragePortlet.xml";
        job1.getPublishersList().add(cocoEmmaPublisher);
        //Build it
        job1.scheduleBuild2(0).get();

        //Do the test
        List<Job> jobs = new LinkedList<Job>();
        jobs.add(job1);
        //Verify the result
        Map<LocalDate, EmmaCoverageResultSummary> summaryMap = EmmaLoadData.loadChartDataWithinRange(jobs, numberOfDays);

        // Testing the size of the returned map against the exepected value,
        // which is a non-zero, therefore tha map must not be empty
        assertEquals(summaryMapSize, summaryMap.size());

        EmmaCoverageResultSummary summary = summaryMap.entrySet().iterator().next().getValue();

        // Test evaluated values against expected ones
        assertEquals(expectedBlockCoverage, summary.getBlockCoverage(), 0.1f);
        assertEquals(expectedClassCoverage, summary.getClassCoverage(), 0.1f);
        assertEquals(expectedLineCoverage, summary.getLineCoverage(), 0.1f);
        assertEquals(expectedMethodCoverage, summary.getMethodCoverage(), 0.1f);
    }

    /**
     * This method tests loadChartDataWithinRange() when it has multiple jobs and a single build.
     * Tests {@link hudson.plugins.cocoemma.portlet.EmmaLoadData#loadChartDataWithinRange(java.util.List, int)}.
     *
     * @throws Exception if so.
     */
    public void testLoadChartDataWithinRangeMultJobsSingleBuild() throws Exception {

        final float expectedBlockCoverage = 0.5f;
        final float expectedClassCoverage = 13.7f;
        final float expectedLineCoverage = 0.6f;
        final float expectedMethodCoverage = 2.0f;
        final int numberOfDays = 1;
        final int summaryMapSize = 1;

        //Create the project
        FreeStyleProject job1 = createFreeStyleProject("job1");

        //Make it do something, in this case it writes a coverage report to the workspace.
        job1.getBuildersList().add(
                new CopyResourceToWorkspaceBuilder(getClass().getResourceAsStream("/hudson/plugins/cocoemma/coveragePortlet.xml"),
                        "reports/coverage/coveragePortlet.xml"));
        //Add a cocoemma publisher
        CocoEmmaPublisher cocoEmmaPublisher = new CocoEmmaPublisher();
        cocoEmmaPublisher.includes = "reports/coverage/coveragePortlet.xml";
        // emmaPublisher.includes = "resources/hudson/plugins/cocoemma/coveragePortlet.xml";
        job1.getPublishersList().add(cocoEmmaPublisher);
        //Build it
        job1.scheduleBuild2(0).get();

        //Do the test
        List<Job> jobs = new LinkedList<Job>();

        FreeStyleProject job2 = createFreeStyleProject("job2");
        jobs.add(job1);
        jobs.add(job2);

        //Verify the result
        Map<LocalDate, EmmaCoverageResultSummary> summaryMap = EmmaLoadData.loadChartDataWithinRange(jobs, numberOfDays);

        // Testing the size of the returned map against the exepected value,
        // which is a non-zero, therefore tha map must not be empty
        assertEquals(summaryMapSize, summaryMap.size());

        EmmaCoverageResultSummary summary = summaryMap.entrySet().iterator().next().getValue();
        // Test evaluated values against expected ones
        assertEquals(expectedBlockCoverage, summary.getBlockCoverage(), 0.1f);
        assertEquals(expectedClassCoverage, summary.getClassCoverage(), 0.1f);
        assertEquals(expectedLineCoverage, summary.getLineCoverage(), 0.1f);
        assertEquals(expectedMethodCoverage, summary.getMethodCoverage(), 0.1f);
    }

    /**
     * This method tests the getResultSummary() behavior.
     * Tests {@link hudson.plugins.cocoemma.portlet.EmmaLoadData#getResultSummary(java.util.Collection)}.
     * @throws Exception if any
     */
    public void testGetResultSummary() throws Exception {

        float blockCoverage = 12.0f;
        float classCoverage = 78.0f;
        float lineCoverage = 82.0f;
        float methodCoverage = 0.7f;
        float conditionCoverage = 0.8f;
        float decisionCoverage = 2.8f;
        float mcdcCoverage = 4.8f;
        float mccCoverage = 5.8f;

        float blockCoverage2 = 54.0f;
        float classCoverage2 = 86.9f;
        float lineCoverage2 = 21.7f;
        float methodCoverage2 = 60.0f;
        float conditionCoverage2 = 0.9f;
        float decisionCoverage2 = 0.4f;
        float mcdcCoverage2 = 0.3f;
        float mccCoverage2 = 1.8f;

        // create a result summary with data from the first cocoemma action
        EmmaCoverageResultSummary coverageResultSummary = new EmmaCoverageResultSummary(
                null, 
                blockCoverage,
                lineCoverage,
                methodCoverage,
                classCoverage,
                decisionCoverage,
                conditionCoverage,
                mcdcCoverage,
                mccCoverage
                );

        // create a result summary with data from the second cocoemma action
        EmmaCoverageResultSummary coverageResultSummary2 = new EmmaCoverageResultSummary(
                null, 
                blockCoverage2, 
                lineCoverage2, 
                methodCoverage2,
                classCoverage2,
                decisionCoverage2,
                conditionCoverage2,
                mcdcCoverage2,
                mccCoverage2
                );

        // add both coverage result summaries to the cocoemma result summary
        EmmaCoverageResultSummary summary = new EmmaCoverageResultSummary();
        summary.addCoverageResult(coverageResultSummary);
        summary.addCoverageResult(coverageResultSummary2);

        // assert the sum has occurred correctly
        assertEquals(blockCoverage + blockCoverage2, summary.getBlockCoverage());
        assertEquals(classCoverage + classCoverage2, summary.getClassCoverage());
        assertEquals(lineCoverage + lineCoverage2, summary.getLineCoverage());
        assertEquals(methodCoverage + methodCoverage2, summary.getMethodCoverage());
        assertEquals(conditionCoverage + conditionCoverage2, summary.getConditionCoverage());
        assertEquals(decisionCoverage + decisionCoverage2, summary.getDecisionCoverage());
        assertEquals(mcdcCoverage + mcdcCoverage2, summary.getMcDcCoverage());
        assertEquals(mccCoverage + mccCoverage2, summary.getMccCoverage());
    }

    /**
     * Test utility class.
     * A Builder that writes some data into a file in the workspace.
     */
    static class CopyResourceToWorkspaceBuilder extends Builder {

        private final InputStream content;
        private final String fileName;

        /**
         * Default constructor.
         *
         * @param content  the content to write to the file.
         * @param fileName the name of the file relative to the workspace.
         */
        CopyResourceToWorkspaceBuilder(InputStream content, String fileName) {
            this.content = content;
            this.fileName = fileName;
        }

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
                throws InterruptedException, IOException {
            FilePath path = build.getWorkspace().child(fileName);
            path.copyFrom(content);
            return true;
        }
    }
}
