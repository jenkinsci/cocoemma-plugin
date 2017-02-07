package hudson.plugins.emma;

import hudson.model.AbstractBuild;
import hudson.util.IOException2;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Root object of the coverage report.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class CoverageReport extends AggregatedReport<CoverageReport/*dummy*/,CoverageReport,PackageReport> {
    private final EmmaBuildAction action;

    private CoverageReport(EmmaBuildAction action) {
        this.action = action;
        setName("SquishCocoEmma");
    }

    public CoverageReport(EmmaBuildAction action, InputStream... xmlReports) throws IOException {
        this(action);
        for (InputStream is: xmlReports) {
          try {
            createDigester().parse(is);
          } catch (SAXException e) {
              throw new IOException2("Failed to parse XML",e);
          }
        }
        setParent(null);
    }

    public CoverageReport(EmmaBuildAction action, File xmlReport) throws IOException {
        this(action);
        try {
            createDigester().parse(xmlReport);
        } catch (SAXException e) {
            throw new IOException2("Failed to parse "+xmlReport,e);
        }
        setParent(null);
    }

    @Override
    public CoverageReport getPreviousResult() {
        EmmaBuildAction prev = action.getPreviousResult();
        if(prev!=null)
            return prev.getResult();
        else
            return null;
    }

    @Override
    public AbstractBuild<?,?> getBuild() {
        return action.owner;
    }

    /**
     * Creates a configured {@link Digester} instance for parsing report XML.
     */
    private Digester createDigester() {
        Digester digester = new Digester();
        digester.setClassLoader(getClass().getClassLoader());

        digester.push(this);

        digester.addObjectCreate( "*/package", PackageReport.class);
        digester.addSetNext(      "*/package","add");
        digester.addSetProperties("*/package");
        digester.addObjectCreate( "*/srcfile", SourceFileReport.class);
        digester.addSetNext(      "*/srcfile","add");
        digester.addSetProperties("*/srcfile");
        digester.addObjectCreate( "*/class", ClassReport.class);
        digester.addSetNext(      "*/class","add");
        digester.addSetProperties("*/class");
        digester.addObjectCreate( "*/method", MethodReport.class);
        digester.addSetNext(      "*/method","add");
        digester.addSetProperties("*/method");

        digester.addObjectCreate("*/coverage", CoverageElement.class);
        digester.addSetProperties("*/coverage");
        digester.addSetNext(      "*/coverage","addCoverage");

        //digester.addObjectCreate("*/testcase",TestCase.class);
        //digester.addSetNext("*/testsuite","add");
        //digester.addSetNext("*/test","add");
        //if(owner.considerTestAsTestObject())
        //    digester.addCallMethod("*/test", "setconsiderTestAsTestObject");
        //digester.addSetNext("*/testcase","add");
        //
        //// common properties applicable to more than one TestObjects.
        //digester.addBeanPropertySetter("*/id");
        //digester.addBeanPropertySetter("*/name");
        //digester.addBeanPropertySetter("*/description");
        //digester.addSetProperties("*/status","value","statusString");  // set attributes. in particular @revision
        //digester.addBeanPropertySetter("*/status","statusMessage");
        return digester;
    }

////////////////////////////////////////////////////////////////////////////////
//  overridden interface implementation for the advanced setup support
//
//      root object for reporting 
//          -> get config data from action object
//              -> read from build.xml???
    
    @Override
    public boolean getTestNotMandatory(){
       return action.getTestNotMandatory();
    }
    
    @Override
    public String getClassDataColumnDescriptor()
    {
        return action.getClassDataColumnDescriptor();
    }

    @Override
    public String getMethodDataColumnDescriptor()
    {
        return action.getMethodDataColumnDescriptor();
    }

    @Override
    public String getBlockDataColumnDescriptor()
    {
        return action.getBlockDataColumnDescriptor();
    }

    @Override
    public String getLineDataColumnDescriptor()
    {
        return action.getLineDataColumnDescriptor();
    }

    @Override
    public String getDecisionDataColumnDescriptor()
    {
        return action.getDecisionDataColumnDescriptor();
    }

    @Override
    public String getConditionDataColumnDescriptor()
    {
        return action.getConditionDataColumnDescriptor();
    }

    @Override
    public String getMcDcDataColumnDescriptor()
    {
        return action.getMcDcDataColumnDescriptor();
    }

    @Override
    public String getMccDataColumnDescriptor()
    {
        return action.getMccDataColumnDescriptor();
    }
//
////////////////////////////////////////////////////////////////////////////////

}
