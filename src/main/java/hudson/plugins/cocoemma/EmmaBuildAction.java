package hudson.plugins.cocoemma;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.util.NullStream;
import hudson.util.StreamTaskListener;

import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerProxy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Build view extension by Emma plugin.
 *
 * As {@link CoverageObject}, it retains the overall coverage report.
 *
 * @author Kohsuke Kawaguchi
 */
public final class EmmaBuildAction extends CoverageObject<EmmaBuildAction> implements HealthReportingAction, StaplerProxy {
	
    public final AbstractBuild<?,?> owner;

    private transient WeakReference<CoverageReport> report;

    /**
     * Non-null if the coverage has pass/fail rules.
     */
    private final Rule rule;

    /**
     * The thresholds that applied when this build was built.
     * @TODO add ability to trend thresholds on the graph
     */
    private final EmmaHealthReportThresholds thresholds;

    public EmmaBuildAction(
            AbstractBuild<?,?> owner, 
            Rule rule, 
            Ratio classCoverage, 
            Ratio methodCoverage, 
            Ratio blockCoverage, 
            Ratio lineCoverage, 
            Ratio decisionCoverage, 
            Ratio conditionCoverage, 
            Ratio mcdcCoverage, 
            Ratio mccCoverage, 
            EmmaHealthReportThresholds thresholds
            ) {
        this.owner = owner;
        this.rule = rule;
        this.clazz = classCoverage;
        this.method = methodCoverage;
        this.block = blockCoverage;
        this.line = lineCoverage;
        this.decision = decisionCoverage;
        this.condition = conditionCoverage;
        this.mcdc = mcdcCoverage;
        this.mcc = mccCoverage;
        this.thresholds = thresholds;
    }

    public String getDisplayName() {
        return Messages.BuildAction_DisplayName();
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getUrlName() {
        return "cocoemma";
    }

    /**
     * Get the coverage {@link hudson.model.HealthReport}.
     *
     * @return The health report or <code>null</code> if health reporting is disabled.
     * @since 1.7
     */
    public HealthReport getBuildHealth() {
        if (thresholds == null) {
            // no thresholds => no report
            return null;
        }
        thresholds.ensureValid();
        int score = 100, percent;
        ArrayList<Localizable> reports = new ArrayList<Localizable>(5);
        if (clazz != null && thresholds.getMaxClass() > 0) {
            percent = clazz.getPercentage(getTestNotMandatory());
            if (percent < thresholds.getMaxClass()) {
                reports.add(Messages._BuildAction_Classes(clazz, percent));
            }
            score = updateHealthScore(score, thresholds.getMinClass(),
                                      percent, thresholds.getMaxClass());
        }
        if (method != null && thresholds.getMaxMethod() > 0) {
            percent = method.getPercentage(getTestNotMandatory());
            if (percent < thresholds.getMaxMethod()) {
                reports.add(Messages._BuildAction_Methods(method, percent));
            }
            score = updateHealthScore(score, thresholds.getMinMethod(),
                                      percent, thresholds.getMaxMethod());
        }
        if (block != null && thresholds.getMaxBlock() > 0) {
            percent = block.getPercentage(getTestNotMandatory());
            if (percent < thresholds.getMaxBlock()) {
                reports.add(Messages._BuildAction_Blocks(block, percent));
            }
            score = updateHealthScore(score, thresholds.getMinBlock(),
                                      percent, thresholds.getMaxBlock());
        }
        if (line != null && thresholds.getMaxLine() > 0) {
            percent = line.getPercentage(getTestNotMandatory());
            if (percent < thresholds.getMaxLine()) {
                reports.add(Messages._BuildAction_Lines(line, percent));
            }
            score = updateHealthScore(score, thresholds.getMinLine(),
                                      percent, thresholds.getMaxLine());
        }
        if (decision != null && thresholds.getMaxDecision() > 0) {
            percent = decision.getPercentage(getTestNotMandatory());
            if (percent < thresholds.getMaxDecision()) {
                reports.add(Messages._BuildAction_Decision(decision, percent));
            }
            score = updateHealthScore(score, thresholds.getMinDecision(),
                                      percent, thresholds.getMaxDecision());
        }
        if (condition != null && thresholds.getMaxCondition() > 0) {
            percent = condition.getPercentage(getTestNotMandatory());
            if (percent < thresholds.getMaxCondition()) {
                reports.add(Messages._BuildAction_Conditions(condition, percent));
            }
            score = updateHealthScore(score, thresholds.getMinCondition(),
                                      percent, thresholds.getMaxCondition());
        }
        if (mcdc != null && thresholds.getMaxMcDc() > 0) {
            percent = mcdc.getPercentage(getTestNotMandatory());
            if (percent < thresholds.getMaxMcDc()) {
                reports.add(Messages._BuildAction_Mcdc(mcdc, percent));
            }
            score = updateHealthScore(score, thresholds.getMinMcDc(),
                                      percent, thresholds.getMaxMcDc());
        }
        if (mcc != null && thresholds.getMaxMcc() > 0) {
            percent = mcc.getPercentage(getTestNotMandatory());
            if (percent < thresholds.getMaxMcc()) {
                reports.add(Messages._BuildAction_Mcc(mcc, percent));
            }
            score = updateHealthScore(score, thresholds.getMinMcc(),
                                      percent, thresholds.getMaxMcc());
        }
        if (score == 100) {
            reports.add(Messages._BuildAction_Perfect());
        }
        // Collect params and replace nulls with empty string
        Object[] args = reports.toArray(new Object[8]);
        for (int i = 7; i >= 0; i--) if (args[i]==null) args[i] = ""; else break;
        
        String description = Messages._BuildAction_Description( args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]).toString() ;
        description = description.trim();
             
        HealthReport report_desc = new HealthReport();
        report_desc.setScore(score);
        report_desc.setDescription(description);
        return report_desc;
    }

    private static int updateHealthScore(int score, int min, int value, int max) {
        if (value >= max) return score;
        if (value <= min) return 0;
        assert max != min;
        final int scaled = (int) (100.0 * ((float) value - min) / (max - min));
        if (scaled < score) return scaled;
        return score;
    }

    public Object getTarget() {
        return getResult();
    }

    @Override
    public AbstractBuild<?,?> getBuild() {
        return owner;
    }
    
	protected static FilePath[] getEmmaReports(File file) throws IOException, InterruptedException {
		FilePath path = new FilePath(file);
		if (path.isDirectory()) {
			return path.list("*xml");
		} else {
			// Read old builds (before 1.11) 
			FilePath report = new FilePath(new File(path.getName() + ".xml"));
			return report.exists() ? new FilePath[]{report} : new FilePath[0];
		}
	}

    /**
     * Obtains the detailed {@link CoverageReport} instance.
     */
    public synchronized CoverageReport getResult() {

        if(report!=null) {
            final CoverageReport r = report.get();
            if(r!=null)     return r;
        }

        final File reportFolder = EmmaPublisher.getEmmaReport(owner);

        try {
        	
        	// Get the list of report files stored for this build
            FilePath[] reports = getEmmaReports(reportFolder);
            InputStream[] streams = new InputStream[reports.length];
            for (int i=0; i<reports.length; i++) {
            	streams[i] = reports[i].read();
            }
            
            // Generate the report
            CoverageReport r = new CoverageReport(this, streams);

            if(rule!=null) {
                // we change the report so that the FAILED flag is set correctly
                logger.info("calculating failed packages based on " + rule);
                rule.enforce(r,new StreamTaskListener(new NullStream()));
            }

            report = new WeakReference<CoverageReport>(r);
            return r;
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Failed to load " + reportFolder, e);
            return null;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load " + reportFolder, e);
            return null;
        }
    }

    @Override
    public EmmaBuildAction getPreviousResult() {
        return getPreviousResult(owner);
    }

    /**
     * Gets the previous {@link EmmaBuildAction} of the given build.
     */
    /*package*/ static EmmaBuildAction getPreviousResult(AbstractBuild<?,?> start) {
        AbstractBuild<?,?> b = start;
        while(true) {
            b = b.getPreviousBuild();
            if(b==null)
                return null;
            if(b.getResult()== Result.FAILURE)
                continue;
            EmmaBuildAction r = b.getAction(EmmaBuildAction.class);
            if(r!=null)
                return r;
        }
    }

    /**
     * Constructs the object from emma XML report files.
     * See <a href="http://emma.sourceforge.net/coverage_sample_c/coverage.xml">an example XML file</a>.
     *
     * @throws IOException
     *      if failed to parse the file.
     */
    public static EmmaBuildAction load(AbstractBuild<?,?> owner, Rule rule, EmmaHealthReportThresholds thresholds, FilePath... files) throws IOException {
        Ratio ratios[] = null;
        for (FilePath f : files) {
            try {
                InputStream in = f.read();
                try {
                    ratios = loadRatios(in, ratios);
                } catch (XmlPullParserException e) {
                    throw new IOException("Failed to parse " + f, e);
                } finally {
                    in.close();
                }
            } catch (java.lang.InterruptedException e) {
                throw new IOException("Failed to parse " + f, e);
            }
        }
        return new EmmaBuildAction(owner,rule,ratios[0],ratios[1],ratios[2],ratios[3],ratios[4],ratios[5],ratios[6],ratios[7],thresholds);
    }

    public static EmmaBuildAction load(AbstractBuild<?,?> owner, Rule rule, EmmaHealthReportThresholds thresholds, InputStream... streams) throws IOException, XmlPullParserException {
        Ratio ratios[] = null;
        for (InputStream in: streams) {
          ratios = loadRatios(in, ratios);
        }
        return new EmmaBuildAction(owner,rule,ratios[0],ratios[1],ratios[2],ratios[3],ratios[4],ratios[5],ratios[6],ratios[7],thresholds);
    }

    private static Ratio[] loadRatios(InputStream in, Ratio[] r) throws IOException, XmlPullParserException {
      
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
      
        XmlPullParser parser = factory.newPullParser();

        parser.setInput(in,null);
        while(true) {
            if(parser.nextTag()!=XmlPullParser.START_TAG)
                continue;
            if(!parser.getName().equals("coverage"))
                continue;
            break;
        }

        if (r == null || r.length < 8) 
            r = new Ratio[8];
        
        // head for the first <coverage> tag.
        for( int i=0; i<r.length; i++ ) {
            if(!parser.getName().equals("coverage"))
                break;  // line coverage is optional

            parser.require(XmlPullParser.START_TAG,"","coverage");
            String v = parser.getAttributeValue("", "value");
            String t = parser.getAttributeValue("", "type");
            
            int index ;
            if ( t.equals("class, %") )
                index = 0;
            else if (t.equals("method, %"))
                index = 1;
            else if ( t.equals("block, %"))
                index = 2;
            else if ( t.equals("line, %") )
                index = 3;
            else if ( t.equals("decision, %") )
                index = 4;
            else if ( t.equals("condition, %") )
                index = 5;
            else if ( t.equals("mcdc, %") )
                index = 6;
            else if ( t.equals("mcc, %") )
                index = 7;
            else
                continue;
                
            
            if (r[index] == null) {
                r[index] = Ratio.parseValue(v);
            } else {
                r[index].addValue(v);
            }
            
            // move to the next coverage tag.
            parser.nextTag();
            parser.nextTag();
        }
        
        return r;

    }

    private static final Logger logger = Logger.getLogger(EmmaBuildAction.class.getName());
}
