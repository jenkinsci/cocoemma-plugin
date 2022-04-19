package hudson.plugins.cocoemma;

import hudson.model.ModelObject;
import hudson.model.Run;

import java.io.IOException;

/**
 * Base class of the coverage report tree,
 * which maintains the details of the coverage report.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractReport<
    PARENT extends AggregatedReport<?,PARENT,?>,
    SELF extends CoverageObject<SELF>> extends CoverageObject<SELF> implements ModelObject {

    private String name;

    private PARENT parent;

    public void addCoverage(CoverageElement cv) throws IOException {
        cv.addTo(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return name;
    }

    /**
     * Called at the last stage of the tree construction,
     * to set the back pointer.
     */
    protected void setParent(PARENT p) {
        this.parent = p;
    }

    /**
     * Gets the back pointer to the parent coverage object.
     */
    public PARENT getParent() {
        return parent;
    }

    @Override
    public SELF getPreviousResult() {
        PARENT p = parent;
        while(true) {
            p = p.getPreviousResult();
            if(p==null)
                return null;
            SELF prev = (SELF)p.getChildren().get(name);
            if(prev!=null)
                return prev;
        }
    }

    @Override
    public Run<?, ?> getBuild() {
        return parent.getBuild();
    }

////////////////////////////////////////////////////////////////////////////////
// default interface implementation for the advanced setup support
    
//    @Override
    public boolean getTestNotMandatory(){
        boolean ret_val = false;
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getTestNotMandatory();
        }
        
        return ret_val;
    }
    
    @Override
    public String getClassDataColumnDescriptor()
    {
        String ret_val = "";
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getClassDataColumnDescriptor();
        }
        
        return ret_val;
    }
    
    @Override
    public String getMethodDataColumnDescriptor()
    {
        String ret_val = "";
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getMethodDataColumnDescriptor();
        }
        
        return ret_val;
    }
    
    @Override
    public String getBlockDataColumnDescriptor()
    {
        String ret_val = "";
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getBlockDataColumnDescriptor();
        }
        
        return ret_val;
    }
    
    @Override
    public String getLineDataColumnDescriptor()
    {
        String ret_val = "";
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getLineDataColumnDescriptor();
        }
        
        return ret_val;
    }
   
    @Override
    public String getConditionDataColumnDescriptor()
    {
        String ret_val = "";
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getConditionDataColumnDescriptor();
        }
        
        return ret_val;
    }
   
    @Override
    public String getMccDataColumnDescriptor()
    {
        String ret_val = "";
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getMccDataColumnDescriptor();
        }
        
        return ret_val;
    }

    @Override
    public String getMcDcDataColumnDescriptor()
    {
        String ret_val = "";
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getMcDcDataColumnDescriptor();
        }
        
        return ret_val;
    }

    @Override
    public String getDecisionDataColumnDescriptor()
    {
        String ret_val = "";
        PARENT p = getParent();
        
        if(p != null){
            ret_val = p.getDecisionDataColumnDescriptor();
        }
        
        return ret_val;
    }
//
////////////////////////////////////////////////////////////////////////////////

}
