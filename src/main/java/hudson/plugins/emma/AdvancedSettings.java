
package hudson.plugins.emma;

import java.io.Serializable;

////////////////////////////////////////////////////////////////////////////////
//  implementation for the advanced setup support
public class AdvancedSettings implements Serializable {
    
    private boolean testNotMandatory = false;


    private String getValue(String value, String defaultValue){   
        
        if ((value == null)||("".equals(value)))
        {
            value = defaultValue;
        }
        return value;
    }

    
    public void setTestNotMandatory(boolean state){
        
        testNotMandatory = state;
    }

    public boolean getTestNotMandatory(){
        
        return testNotMandatory;
    }
    
    public String getClassDataColumnDescriptor() {   
        
        return Messages.CoverageObject_Legend_Class();
    }


    public String getMethodDataColumnDescriptor() {
        
        return Messages.CoverageObject_Legend_Method();
    }
    
 
    public String getBlockDataColumnDescriptor() {
        
        return Messages.CoverageObject_Legend_Block();
    }
    
     public String getLineDataColumnDescriptor() {
        
        return Messages.CoverageObject_Legend_Line();
    }
    
    public String getConditionDataColumnDescriptor() {
        
        return Messages.CoverageObject_Legend_Condition();
    }
    
    public String getDecisionDataColumnDescriptor() {
        
        return Messages.CoverageObject_Legend_Decision();
    }
    
    public String getMcDcDataColumnDescriptor() {
        
        return Messages.CoverageObject_Legend_Mcdc();
    }
    
    public String getMccDataColumnDescriptor() {
        
        return  Messages.CoverageObject_Legend_Mcc();
    }
    

    
    public void applySettings(AdvancedSettings settings){
        
        if(settings != null){
            setTestNotMandatory(settings.getTestNotMandatory());
        }
    }    
}
//
////////////////////////////////////////////////////////////////////////////////
