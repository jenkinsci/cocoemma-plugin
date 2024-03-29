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
package hudson.plugins.cocoemma.portlet.chart;

import hudson.plugins.cocoemma.portlet.EmmaLoadData;
import hudson.plugins.cocoemma.portlet.Messages;
import hudson.plugins.cocoemma.portlet.bean.EmmaCoverageResultSummary;
import hudson.plugins.cocoemma.portlet.utils.Constants;
import hudson.plugins.cocoemma.portlet.utils.Utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import java.time.LocalDate;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A portlet for Emma coverage results - Trend Chart.
 */
public class EmmaBuilderTrendChart extends DashboardPortlet {

  /**
   * Chart width that can be set by user.
   */
  private final int width;

  /**
   * Chart height that can be set by user.
   */
  private final int height;

  /**
   * Number of days of the chart that can be set by user.
   */
  private final int daysNumber;

  /**
   * Constructor with chart attributes as parameters.
   * DataBoundConstructor annotation helps the Stapler class to find
   * which constructor that should be used when automatically copying
   * values from a web form to a class.
   *
   * @param name
   *          chart name
   * @param width
   *          the chart width
   * @param height
   *          the chart height
   * @param daysNumber
   *          the number of days
   */
  @DataBoundConstructor
  public EmmaBuilderTrendChart(String name, String width, String height, String daysNumber) {

    super(name);

    this.width = Utils.validateChartAttributes(width, Constants.DEFAULT_WIDTH);
    this.height = Utils.validateChartAttributes(height, Constants.DEFAULT_HEIGHT);
    this.daysNumber = Utils.validateChartAttributes(daysNumber, Constants.DEFAULT_DAYS_NUMBER);
  }

  /**
   * This method will be called by portlet.jelly to load data and
   * create the chart.
   *
   * @return Graph a summary graph
   */
  public Graph getSummaryGraph() {

    Map<LocalDate, EmmaCoverageResultSummary> summaries;

    // Retrieve Dashboard View jobs
    List<Job> jobs = getDashboard().getJobs();

    // Fill a HashMap with the data will be showed in the chart
    summaries = EmmaLoadData.loadChartDataWithinRange(jobs, daysNumber);

    return createTrendChart(summaries, width, height);
  }

  /**
   * Creates a graph for Emma Coverage results.
   *
   * @param summaries
   *          HashMap(key = run date and value = Instrumentation tests
   *          results)
   * @param widthParam
   *          the chart width
   * @param heightParam
   *          the chart height
   * @return Graph (JFreeChart)
   */
  private static Graph createTrendChart(final Map<LocalDate, EmmaCoverageResultSummary> summaries, int widthParam,
    int heightParam) {

    return new Graph(-1, widthParam, heightParam) {

      @Override
      protected JFreeChart createGraph() {

        // Show empty chart
        if (summaries == null) {
          JFreeChart chart = ChartFactory.createStackedAreaChart(null, Constants.AXIS_LABEL,
            Constants.AXIS_LABEL_VALUE, null, PlotOrientation.VERTICAL, true, false, false);

          return chart;
        }

        int lineNumber = 0;

        JFreeChart chart = ChartFactory.createLineChart("", Constants.AXIS_LABEL, Constants.AXIS_LABEL_VALUE,
          buildDataSet(summaries), PlotOrientation.VERTICAL, true, false, false);

        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = chart.getCategoryPlot();

        // Line thickness
        CategoryItemRenderer renderer = plot.getRenderer();
        BasicStroke stroke = new BasicStroke(Constants.LINE_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        renderer.setSeriesStroke(lineNumber++, stroke);
        renderer.setSeriesStroke(lineNumber++, stroke);
        renderer.setSeriesStroke(lineNumber++, stroke);
        renderer.setSeriesStroke(lineNumber, stroke);

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(Constants.FOREGROUND_ALPHA);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(Constants.DEFAULT_MARGIN);
        domainAxis.setUpperMargin(Constants.DEFAULT_MARGIN);
        domainAxis.setCategoryMargin(Constants.DEFAULT_MARGIN);

        NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(Constants.UPPER_BOUND);
        rangeAxis.setLowerBound(Constants.LOWER_BOUND);

        return chart;

      }
    };
  }

  /**
   * Build data set.
   *
   * @param summaries
   *          HashMap containing data of chart.
   * @return CategoryDataset Interface for a dataset with one or more
   *         series, and values associated with categories.
   */
  private static CategoryDataset buildDataSet(Map<LocalDate, EmmaCoverageResultSummary> summaries) {

    DataSetBuilder<String, LocalDate> dataSetBuilder = new DataSetBuilder<String, LocalDate>();

    for (Map.Entry<LocalDate, EmmaCoverageResultSummary> entry : summaries.entrySet()) {
      float blockCoverage = 0;
      float classCoverage = 0;
      float lineCoverage = 0;
      float methodCoverage = 0;
      float decisionCoverage = 0;
      float conditionCoverage = 0;
      float mcdcCoverage = 0;
      float mccCoverage = 0;

      int count = 0;

      List<EmmaCoverageResultSummary> list = entry.getValue().getEmmaCoverageResults();

      for (EmmaCoverageResultSummary item : list) {
        blockCoverage += item.getBlockCoverage();
        classCoverage += item.getClassCoverage();
        lineCoverage += item.getLineCoverage();
        methodCoverage += item.getMethodCoverage();
        decisionCoverage += item.getDecisionCoverage();
        conditionCoverage += item.getConditionCoverage();
        mcdcCoverage += item.getMcDcCoverage();
        mccCoverage += item.getMccCoverage();
        count++;
      }

      dataSetBuilder.add((blockCoverage / count), "block", entry.getKey());
      dataSetBuilder.add((classCoverage / count), "class", entry.getKey());
      dataSetBuilder.add((lineCoverage / count), "line", entry.getKey());
      dataSetBuilder.add((methodCoverage / count), "method", entry.getKey());
      dataSetBuilder.add((decisionCoverage / count), "decision", entry.getKey());
      dataSetBuilder.add((conditionCoverage / count), "condition", entry.getKey());
      dataSetBuilder.add((mcdcCoverage / count), "MC/DC", entry.getKey());
      dataSetBuilder.add((mccCoverage / count), "xMCC", entry.getKey());
    }

    return dataSetBuilder.build();
  }

  /**
   * Descriptor that will be shown on Dashboard Portlets view.
   */
  @Extension(optional = true)
  public static class DescriptorImpl extends Descriptor<DashboardPortlet> {

    @Override
    public String getDisplayName() {
      return Messages.chartTitle();
    }
  }

  /**
   * Getter of the width.
   *
   * @return int the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Getter of the height.
   *
   * @return int the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Getter of the number of days.
   *
   * @return int the number of days
   */
  public int getDaysNumber() {
    return daysNumber;
  }
}
