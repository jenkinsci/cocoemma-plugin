package hudson.plugins.cocoemma;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * Project view extension by Emma plugin.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class CocoEmmaProjectAction implements Action {
    public final AbstractProject<?,?> project;

    public CocoEmmaProjectAction(AbstractProject project) {
        this.project = project;
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getDisplayName() {
        return Messages.ProjectAction_DisplayName();
    }

    public String getUrlName() {
        return "cocoemma";
    }

    /**
     * Gets the most recent {@link CocoEmmaBuildAction} object.
     */
    public CocoEmmaBuildAction getLastResult() {
        for( AbstractBuild<?,?> b = project.getLastBuild(); b!=null; b=b.getPreviousBuild()) {
            if(b.getResult()== Result.FAILURE)
                continue;
            CocoEmmaBuildAction r = b.getAction(CocoEmmaBuildAction.class);
            if(r!=null)
                return r;
        }
        return null;
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
       if (getLastResult() != null)
          getLastResult().doGraph(req,rsp);
    }
}
