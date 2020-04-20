package hudson.plugins.cocoemma.rules;

import hudson.model.TaskListener;
import hudson.plugins.cocoemma.Rule;
import hudson.plugins.cocoemma.CoverageReport;
import hudson.plugins.cocoemma.PackageReport;
import hudson.plugins.cocoemma.SourceFileReport;

/**
 * Flags a failure if the line coverage of a source file
 * goes below a certain threashold.
 */
public class LineCoveragePerSourceFileRule extends Rule {

    private static final long serialVersionUID = -2869893039051762047L;

    private final float minPercentage;

    public LineCoveragePerSourceFileRule(float minPercentage) {
        this.minPercentage = minPercentage;
    }

    public void enforce(CoverageReport report, TaskListener listener) {
        for (PackageReport pack : report.getChildren().values()) {
            for (SourceFileReport sfReport : pack.getChildren().values()) {
                float percentage = sfReport.getLineCoverage().getPercentageFloat(report.getTestNotMandatory());

                if (percentage < minPercentage) {
                    listener.getLogger().println("Squish Coco Emma: " + sfReport.getDisplayName() + " failed (below " + minPercentage + "%).");
                    sfReport.setFailed();
                }
            }
        }
    }
}
