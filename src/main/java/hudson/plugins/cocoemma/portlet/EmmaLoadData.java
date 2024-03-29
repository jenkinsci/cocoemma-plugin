/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

/**
 * @author Allyn Pierre (Allyn.GreyDeAlmeidaLimaPierre@sonyericsson.com)
 * @author Eduardo Palazzo (Eduardo.Palazzo@sonyericsson.com)
 * @author Mauro Durante (Mauro.DuranteJunior@sonyericsson.com)
 */
package hudson.plugins.cocoemma.portlet;

import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.cocoemma.CocoEmmaBuildAction;
import hudson.plugins.cocoemma.portlet.bean.EmmaCoverageResultSummary;
import hudson.plugins.cocoemma.portlet.utils.Utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Load data of Emma coverage results used by chart or grid.
 */
public final class EmmaLoadData {

  /**
   * Private constructor avoiding this class to be used in a non-static way.
   */
  private EmmaLoadData() {
  }

  /**
   * Get Emma coverage results of all jobs and store into a sorted
   * HashMap by date.
   *
   * @param jobs
   *        jobs of Dashboard view
   * @param daysNumber
   *          number of days
   * @return Map The sorted summaries
   */
  public static Map<LocalDate, EmmaCoverageResultSummary> loadChartDataWithinRange(List<Job> jobs, int daysNumber) {

    Map<LocalDate, EmmaCoverageResultSummary> summaries = new HashMap<LocalDate, EmmaCoverageResultSummary>();

    // Get the last build (last date) of the all jobs
    LocalDate lastDate = Utils.getLastDate(jobs);

    // No builds
    if (lastDate == null) {
      return null;
    }

    // Get the first date from last build date minus number of days
    LocalDate firstDate = lastDate.minusDays(daysNumber);

    // For each job, get Emma coverage results according with
    // date range (last build date minus number of days)
    for (Job job : jobs) {

      Run run = job.getLastBuild();

      if (null != run) {
        LocalDate runDate = LocalDate.ofEpochDay(run.getTimestamp().toInstant().getEpochSecond());

        while (runDate.isAfter(firstDate)) {

          summarize(summaries, run, runDate, job);

          run = run.getPreviousBuild();

          if (null == run) {
            break;
          }

          runDate = LocalDate.ofEpochDay(run.getTimestamp().toInstant().getEpochSecond());

        }
      }
    }

    // Sorting by date, ascending order
    Map<LocalDate, EmmaCoverageResultSummary> sortedSummaries = new TreeMap(summaries);

    return sortedSummaries;

  }

  /**
   * Summarize Emma converage results.
   *
   * @param summaries
   *          a Map of EmmaCoverageResultSummary objects indexed by
   *          dates
   * @param run
   *          the build which will provide information about the
   *          coverage result
   * @param runDate
   *          the date on which the build was performed
   * @param job
   *          job from the DashBoard Portlet view
   */
  private static void summarize(Map<LocalDate, EmmaCoverageResultSummary> summaries, Run run, LocalDate runDate, Job job) {

    EmmaCoverageResultSummary emmaCoverageResult = getResult(run);

    // Retrieve Emma information for informed date
    EmmaCoverageResultSummary emmaCoverageResultSummary = summaries.get(runDate);

    // Consider the last result of each
    // job date (if there are many builds for the same date). If not
    // exists, the Emma coverage data must be added. If exists
    // Emma coverage data for the same date but it belongs to other
    // job, sum the values.
    if (emmaCoverageResultSummary == null) {
      emmaCoverageResultSummary = new EmmaCoverageResultSummary();
      emmaCoverageResultSummary.addCoverageResult(emmaCoverageResult);
      emmaCoverageResultSummary.setJob(job);
    } else {

      // Check if exists Emma data for same date and job
      List<EmmaCoverageResultSummary> listResults = emmaCoverageResultSummary.getEmmaCoverageResults();
      boolean found = false;

      for (EmmaCoverageResultSummary item : listResults) {
        if ((null != item.getJob()) && (null != item.getJob().getName()) && (null != job)) {
          if (item.getJob().getName().equals(job.getName())) {
            found = true;
            break;
          }
        }
      }

      if (!found) {
        emmaCoverageResultSummary.addCoverageResult(emmaCoverageResult);
        emmaCoverageResultSummary.setJob(job);
      }
    }

    summaries.put(runDate, emmaCoverageResultSummary);
  }

  /**
   * Get the Emma coverage result for a specific run.
   *
   * @param run
   *          a job execution
   * @return EmmaCoverageTestResult the coverage result
   */
  private static EmmaCoverageResultSummary getResult(Run run) {
    CocoEmmaBuildAction cocoEmmaAction = run.getAction(CocoEmmaBuildAction.class);

    float blockCoverage = 0.0f;
    float classCoverage = 0.0f;
    float lineCoverage = 0.0f;
    float methodCoverage = 0.0f;
    float decisionCoverage = 0.0f;
    float conditionCoverage = 0.0f;
    float mcdcCoverage = 0.0f;
    float mccCoverage = 0.0f;

    if (cocoEmmaAction != null) {
      if (null != cocoEmmaAction.getBlockCoverage()) {
        blockCoverage = cocoEmmaAction.getBlockCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
      }
      if (null != cocoEmmaAction.getClassCoverage()) {
        classCoverage = cocoEmmaAction.getClassCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
      }
      if (null != cocoEmmaAction.getLineCoverage()) {
        lineCoverage = cocoEmmaAction.getLineCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
      }
      if (null != cocoEmmaAction.getMethodCoverage()) {
        methodCoverage = cocoEmmaAction.getMethodCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
      }
      if (null != cocoEmmaAction.getDecisionCoverage()) {
        decisionCoverage = cocoEmmaAction.getDecisionCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
      }
      if (null != cocoEmmaAction.getConditionCoverage()) {
        conditionCoverage = cocoEmmaAction.getConditionCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
      }
      if (null != cocoEmmaAction.getMcDcCoverage()) {
        mcdcCoverage = cocoEmmaAction.getMcDcCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
      }
      if (null != cocoEmmaAction.getMccCoverage()) {
        mccCoverage = cocoEmmaAction.getMccCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
      }
    }
    return new EmmaCoverageResultSummary(
            run.getParent(), 
            blockCoverage,
            lineCoverage, 
            methodCoverage, 
            classCoverage, 
            decisionCoverage,
            conditionCoverage,
            mcdcCoverage,
            mccCoverage
            );
  }

  /**
   * Summarize the last coverage results of all jobs, which have coverage.
   *
   * @param jobs
   *          a final Collection of Job objects
   * @return EmmaCoverageResultSummary the result summary
   */
  public static EmmaCoverageResultSummary getResultSummary(final Collection<Job> jobs) {
    EmmaCoverageResultSummary summary = new EmmaCoverageResultSummary();

    for (Job job : jobs) {

      float blockCoverage = 0.0f;
      float classCoverage = 0.0f;
      float lineCoverage = 0.0f;
      float methodCoverage = 0.0f;
      float decisionCoverage = 0.0f;
      float conditionCoverage = 0.0f;
      float mcdcCoverage = 0.0f;
      float mccCoverage = 0.0f;

      Run run = job.getLastSuccessfulBuild();

      if (run != null) {

        CocoEmmaBuildAction cocoEmmaAction = job.getLastSuccessfulBuild().getAction(CocoEmmaBuildAction.class);

        if (null == cocoEmmaAction) {
            continue;
        } else {
          if (null != cocoEmmaAction.getBlockCoverage()) {
            blockCoverage = cocoEmmaAction.getBlockCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
            BigDecimal bigBlockCoverage = new BigDecimal(blockCoverage);
            bigBlockCoverage = bigBlockCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            blockCoverage = bigBlockCoverage.floatValue();
          }

          if (null != cocoEmmaAction.getClassCoverage()) {
            classCoverage = cocoEmmaAction.getClassCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
            BigDecimal bigClassCoverage = new BigDecimal(classCoverage);
            bigClassCoverage = bigClassCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            classCoverage = bigClassCoverage.floatValue();
          }
          if (null != cocoEmmaAction.getLineCoverage()) {
            lineCoverage = cocoEmmaAction.getLineCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
            BigDecimal bigLineCoverage = new BigDecimal(lineCoverage);
            bigLineCoverage = bigLineCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            lineCoverage = bigLineCoverage.floatValue();
          }

          if (null != cocoEmmaAction.getMethodCoverage()) {
            methodCoverage = cocoEmmaAction.getMethodCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
            BigDecimal bigMethodCoverage = new BigDecimal(methodCoverage);
            bigMethodCoverage = bigMethodCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            methodCoverage = bigMethodCoverage.floatValue();
          }
          
          if (null != cocoEmmaAction.getDecisionCoverage()) {
            decisionCoverage = cocoEmmaAction.getDecisionCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
            BigDecimal bigCoverage = new BigDecimal(decisionCoverage);
            bigCoverage = bigCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            decisionCoverage = bigCoverage.floatValue();
          }
          
          if (null != cocoEmmaAction.getConditionCoverage()) {
            conditionCoverage = cocoEmmaAction.getConditionCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
            BigDecimal bigCoverage = new BigDecimal(conditionCoverage);
            bigCoverage = bigCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            conditionCoverage = bigCoverage.floatValue();
          }
          if (null != cocoEmmaAction.getMcDcCoverage()) {
            mcdcCoverage = cocoEmmaAction.getMcDcCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
            BigDecimal bigCoverage = new BigDecimal(mcdcCoverage);
            bigCoverage = bigCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            mcdcCoverage = bigCoverage.floatValue();
          }
          if (null != cocoEmmaAction.getMccCoverage()) {
            mccCoverage = cocoEmmaAction.getMccCoverage().getPercentageFloat(cocoEmmaAction.getTestNotMandatory());
            BigDecimal bigCoverage = new BigDecimal(mccCoverage);
            bigCoverage = bigCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            mccCoverage = bigCoverage.floatValue();
          }
        }
      }
      summary.addCoverageResult(
              new EmmaCoverageResultSummary(
                  job, 
                  blockCoverage, 
                  lineCoverage, 
                  methodCoverage,
                  classCoverage, 
                  decisionCoverage,
                  conditionCoverage,
                  mcdcCoverage,
                  mccCoverage
                  )
              );
    }
    return summary;
  }
}
