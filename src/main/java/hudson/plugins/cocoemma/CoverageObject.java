package hudson.plugins.cocoemma;

import hudson.Util;
import hudson.model.Api;
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.Graph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Base class of all coverage objects.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public abstract class CoverageObject<SELF extends CoverageObject<SELF>> extends AdvancedSettings {

    Ratio clazz = new Ratio();
    Ratio method = new Ratio();
    Ratio block = new Ratio();
    Ratio line = new Ratio();
    Ratio decision = new Ratio();
    Ratio condition = new Ratio();
    Ratio mcdc = new Ratio();
    Ratio mcc = new Ratio();
    
    private volatile boolean failed = false;

    public boolean isFailed() {
        return failed;
    }

    /**
     * Marks this coverage object as failed.
     * @see Rule
     */
    public void setFailed() {
        failed = true;
    }

    @Exported(inline=true)
    public Ratio getClassCoverage() {
        return clazz;
    }

    @Exported(inline=true)
    public Ratio getMethodCoverage() {
        return method;
    }

    @Exported(inline=true)
    public Ratio getBlockCoverage() {
        return block;
    }

    /**
     * Line coverage. Can be null if this information is not collected.
     */
    @Exported(inline=true)
    public Ratio getLineCoverage() {
        return line;
    }

    /**
     * Decision coverage. Can be null if this information is not collected.
     */
    @Exported(inline=true)
    public Ratio getDecisionCoverage() {
        return decision;
    }

    /**
     * Condition coverage. Can be null if this information is not collected.
     */
    @Exported(inline=true)
    public Ratio getConditionCoverage() {
        return condition;
    }

    /**
     * MC/DC coverage. Can be null if this information is not collected.
     */
    @Exported(inline=true)
    public Ratio getMcDcCoverage() {
        return mcdc;
    }

    /**
     * MCC coverage. Can be null if this information is not collected.
     */
    @Exported(inline=true)
    public Ratio getMccCoverage() {
        return mcc;
    }

    /**
     * Gets the build object that owns the whole coverage report tree.
     */
    public abstract Run<?,?> getBuild();

    /**
     * Gets the corresponding coverage report object in the previous
     * run that has the record.
     *
     * @return
     *      null if no earlier record was found.
     */
    @Exported
    public abstract SELF getPreviousResult();

    /**
     * Used in the view to print out four table columns with the coverage info.
     */
    public String printFourCoverageColumns() {
        StringBuilder buf = new StringBuilder();
        printRatioCell(isFailed(), clazz, buf, getTestNotMandatory());
        printRatioCell(isFailed(), method, buf, getTestNotMandatory());
        printRatioCell(isFailed(), block, buf, getTestNotMandatory());
        printRatioCell(isFailed(), line, buf, getTestNotMandatory());
        printRatioCell(isFailed(), decision, buf, getTestNotMandatory());
        printRatioCell(isFailed(), condition, buf, getTestNotMandatory());
        printRatioCell(isFailed(), mcdc, buf, getTestNotMandatory());
        printRatioCell(isFailed(), mcc, buf, getTestNotMandatory());
        return buf.toString();
    }

    public boolean hasMcDcCoverage() {
        return mcdc.isInitialized();
    }

    public boolean hasMccCoverage() {
        return mcc.isInitialized();
    }

    public boolean hasDecisionCoverage() {
        return decision.isInitialized();
    }

    public boolean hasConditionCoverage() {
        return condition.isInitialized();
    }

    public boolean hasLineCoverage() {
        return line.isInitialized();
    }

    public boolean hasClassCoverage() {
        return clazz.isInitialized();
    }

    
    static NumberFormat dataFormat = new DecimalFormat("000.00");
    static NumberFormat percentFormat = new DecimalFormat("0.0");
    static NumberFormat intFormat = new DecimalFormat("0");
    
	protected static void printRatioCell(boolean failed, Ratio ratio, StringBuilder buf, boolean no_tests_required) {
                if (ratio != null && ratio.isInitialized()) {
			String className = "nowrap" + (failed ? " red" : "");
			buf.append("<td class='").append(className).append("'");
			buf.append(" data='").append(dataFormat.format(ratio.getPercentageFloat(no_tests_required)));
			buf.append("'>\n");
			printRatioTable(ratio, buf, no_tests_required);
			buf.append("</td>\n");
		}
	}
	
	protected static void printRatioTable(Ratio ratio, StringBuilder buf, boolean no_tests_required){
		String percent = percentFormat.format(ratio.getPercentageFloat(no_tests_required));
		String numerator = intFormat.format(ratio.getNumerator());
		String denominator = intFormat.format(ratio.getDenominator());
		buf.append("<table class='percentgraph' cellpadding='0px' cellspacing='0px'><tr class='percentgraph'>")
				.append("<td width='64px' class='data'>").append(percent).append("%</td>")
				.append("<td class='percentgraph'>")
				.append("<div class='percentgraph'><div class='greenbar' style='width: ").append(ratio.getPercentageFloat(no_tests_required)).append("px;'>")
				.append("<span class='text'>").append(numerator).append("/").append(denominator)
				.append("</span></div></div></td></tr></table>") ;
	}

    /**
     * Generates the graph that shows the coverage trend up to this report.
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if(ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath()+"/images/headless.png");
            return;
        }

        Run<?,?> build = getBuild();
        Calendar t = build.getTimestamp();

        String w = Util.fixEmptyAndTrim(req.getParameter("width"));
        String h = Util.fixEmptyAndTrim(req.getParameter("height"));
        int width = (w != null) ? Integer.parseInt(w) : 500;
        int height = (h != null) ? Integer.parseInt(h) : 200;

        new GraphImpl(this, t, width, height) {

            @Override
            protected DataSetBuilder<String, NumberOnlyBuildLabel> createDataSet(CoverageObject<SELF> obj) {
                DataSetBuilder<String, NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, NumberOnlyBuildLabel>();

                for (CoverageObject<SELF> a = obj; a != null; a = a.getPreviousResult()) {                  
                    NumberOnlyBuildLabel label = new NumberOnlyBuildLabel( (Run)a.getBuild());
                    dsb.add(a.clazz.getPercentageFloat(getTestNotMandatory()), a.getClassDataColumnDescriptor(), label);
                    dsb.add(a.method.getPercentageFloat(getTestNotMandatory()), a.getMethodDataColumnDescriptor(), label);
                    dsb.add(a.block.getPercentageFloat(getTestNotMandatory()), a.getBlockDataColumnDescriptor(), label);
                    if (a.line != null) {
                        if (a.hasLineCoverage()){
                            dsb.add(a.line.getPercentageFloat(getTestNotMandatory()), a.getLineDataColumnDescriptor(), label);
                        }
                    }
                    if (a.decision != null) {
                        if (a.hasDecisionCoverage()){
                            dsb.add(a.decision.getPercentageFloat(getTestNotMandatory()), a.getDecisionDataColumnDescriptor(), label);
                        }
                    }
                    if (a.condition != null) {
                        if (a.hasConditionCoverage()){
                            dsb.add(a.condition.getPercentageFloat(getTestNotMandatory()), a.getConditionDataColumnDescriptor(), label);
                        }
                    }
                    if (a.mcdc != null) {
                        if (a.hasMcDcCoverage()){
                            dsb.add(a.mcdc.getPercentageFloat(getTestNotMandatory()), a.getMcDcDataColumnDescriptor(), label);
                        }
                    }
                    if (a.mcc != null) {
                        if (a.hasMccCoverage()){
                            dsb.add(a.mcc.getPercentageFloat(getTestNotMandatory()), a.getMccDataColumnDescriptor(), label);
                        }
                    }
                }

                return dsb;
            }
        }.doPng(req, rsp);
    }

    public Api getApi() {
    	return new Api(this);
    }

    private abstract class GraphImpl extends Graph {

        private CoverageObject<SELF> obj;

        public GraphImpl(CoverageObject<SELF> obj, Calendar timestamp, int defaultW, int defaultH) {
            super(timestamp, defaultW, defaultH);
            this.obj = obj;
        }

        protected abstract DataSetBuilder<String, NumberOnlyBuildLabel> createDataSet(CoverageObject<SELF> obj);

        protected JFreeChart createGraph() {
            final CategoryDataset dataset = createDataSet(obj).build();
            final JFreeChart chart = ChartFactory.createLineChart(
                    null, // chart title
                    null, // unused
                    "%", // range axis label
                    dataset, // data
                    PlotOrientation.VERTICAL, // orientation
                    true, // include legend
                    true, // tooltips
                    false // urls
                    );

            // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

            final LegendTitle legend = chart.getLegend();
            legend.setPosition(RectangleEdge.RIGHT);

            chart.setBackgroundPaint(Color.white);

            final CategoryPlot plot = chart.getCategoryPlot();

            // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlinePaint(null);
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.black);

            CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
            plot.setDomainAxis(domainAxis);
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            domainAxis.setLowerMargin(0.0);
            domainAxis.setUpperMargin(0.0);
            domainAxis.setCategoryMargin(0.0);

            final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setUpperBound(100);
            rangeAxis.setLowerBound(0);

            final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
            renderer.setBaseStroke(new BasicStroke(4.0f));
            ColorPalette.apply(renderer);

            // crop extra space around the graph
            plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

            return chart;
        }
    }
}
