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
package hudson.plugins.cocoemma.portlet.grid;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.cocoemma.portlet.EmmaLoadData;
import hudson.plugins.cocoemma.portlet.Messages;
import hudson.plugins.cocoemma.portlet.bean.EmmaCoverageResultSummary;
import hudson.plugins.view.dashboard.DashboardPortlet;

import java.util.Collection;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A portlet for Emma Coverage results - Grid data.
 *
 * see http://wiki.hudson-ci.org/display/HUDSON/Dashboard+View
 */
public class EmmaBuilderGrid extends DashboardPortlet {

  /**
   * Constructor with grid name as parameter. DataBoundConstructor
   * annotation helps the Stapler class to find which constructor that
   * should be used when automatically copying values from a web form
   * to a class.
   *
   * @param name
   *          grid name
   */
  @DataBoundConstructor
  public EmmaBuilderGrid(String name) {
    super(name);
  }

  /**
   * This method will be called by portlet.jelly to load data and
   * create the grid.
   *
   * @param jobs
   *          a Collection of Job objects
   * @return EmmaCoverageResultSummary a coverage result summary
   */
  public EmmaCoverageResultSummary getEmmaCoverageResultSummary(Collection<Job> jobs) {
    return EmmaLoadData.getResultSummary(jobs);
  }

  /**
   * Descriptor that will be shown on Dashboard Portlets view.
   */
  @Extension(optional = true)
  public static class EmmaGridDescriptor extends Descriptor<DashboardPortlet> {

    @Override
    public String getDisplayName() {
      return Messages.gridTitle();
    }
  }
}
