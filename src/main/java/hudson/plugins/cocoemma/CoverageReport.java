package hudson.plugins.cocoemma;

import hudson.model.Run;

import org.apache.commons.digester3.Digester;
import javax.xml.parsers.ParserConfigurationException;
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
    private final CocoEmmaBuildAction action;

    private CoverageReport(CocoEmmaBuildAction action) {
        this.action = action;
        setName("SquishCocoEmma");
    }

    public CoverageReport(CocoEmmaBuildAction action, InputStream... xmlReports) throws IOException {
        this(action);
        for (InputStream is: xmlReports) {
          try {
            createDigester(!Boolean.getBoolean(this.getClass().getName() + ".UNSAFE")).parse(is);
          } catch (SAXException e) {
              throw new IOException("Failed to parse XML",e);
          }
        }
        setParent(null);
    }

    public CoverageReport(CocoEmmaBuildAction action, File xmlReport) throws IOException {
        this(action);
        try {
            createDigester(!Boolean.getBoolean(this.getClass().getName() + ".UNSAFE")).parse(xmlReport);
        } catch (SAXException e) {
            throw new IOException("Failed to parse "+xmlReport,e);
        }
        setParent(null);
    }

    @Override
    public CoverageReport getPreviousResult() {
        CocoEmmaBuildAction prev = action.getPreviousResult();
        if(prev!=null)
            return prev.getResult();
        else
            return null;
    }

    @Override
    public Run<?,?> getBuild() {
        return action.owner;
    }

    /**
     * Creates a configured {@link Digester} instance for parsing report XML.
     */
    private Digester createDigester(boolean secure) throws SAXException {
        Digester digester = new Digester();

        if (secure) {
            digester.setXIncludeAware(false);
            try {
                digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                digester.setFeature("http://xml.org/sax/features/external-general-entities", false);
                digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                digester.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (ParserConfigurationException ex) {
                throw new SAXException("Failed to securely configure xml digester parser", ex);
            }
        }
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
