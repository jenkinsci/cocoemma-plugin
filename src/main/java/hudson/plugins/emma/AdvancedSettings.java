
package hudson.plugins.emma;

import java.io.Serializable;

////////////////////////////////////////////////////////////////////////////////
//  implementation for the advanced setup support
public class AdvancedSettings implements Serializable {
    
    private boolean testNotMandatory = false;

    private String classDataColumnDescriptor = "";
    private String methodDataColumnDescriptor = "";
    private String blockDataColumnDescriptor = "";
    private String lineDataColumnDescriptor = "";
    private String decisionDataColumnDescriptor = "";
    private String conditionDataColumnDescriptor = "";
    private String mcdcDataColumnDescriptor = "";
    private String mccDataColumnDescriptor = "";


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
        
        return getValue(classDataColumnDescriptor, Messages.CoverageObject_Legend_Class());
    }
    
    public void setClassDataColumnDescriptor(String name) {
        
        classDataColumnDescriptor = name;
    }

    public String getMethodDataColumnDescriptor() {
        
        return getValue(methodDataColumnDescriptor, Messages.CoverageObject_Legend_Block());
    }
    
    public void setMethodDataColumnDescriptor(String name) {
        
        methodDataColumnDescriptor = name;
    }

    public String getBlockDataColumnDescriptor() {
        
        return getValue(blockDataColumnDescriptor, Messages.CoverageObject_Legend_Method());
    }
    
    public void setBlockDataColumnDescriptor(String name) {
        
        blockDataColumnDescriptor = name;
    }

    public String getLineDataColumnDescriptor() {
        
        return getValue(lineDataColumnDescriptor, Messages.CoverageObject_Legend_Line());
    }
    
    public void setLineDataColumnDescriptor(String name) {
        
        lineDataColumnDescriptor = name;
    }

    public String getConditionDataColumnDescriptor() {
        
        return getValue(conditionDataColumnDescriptor, Messages.CoverageObject_Legend_Condition());
    }
    
    public void setConditionDataColumnDescriptor(String name) {
        
        conditionDataColumnDescriptor = name;
    }
    
    public String getDecisionDataColumnDescriptor() {
        
        return getValue(decisionDataColumnDescriptor, Messages.CoverageObject_Legend_Decision());
    }
    
    public void setDecisionDataColumnDescriptor(String name) {
        
        decisionDataColumnDescriptor = name;
    }
    
    public String getMcDcDataColumnDescriptor() {
        
        return getValue(mcdcDataColumnDescriptor, Messages.CoverageObject_Legend_Mcdc());
    }
    
    public void setMccDataColumnDescriptor(String name) {
        
        mccDataColumnDescriptor = name;
    }
    
    public String getMccDataColumnDescriptor() {
        
        return getValue(mccDataColumnDescriptor, Messages.CoverageObject_Legend_Mcc());
    }
    
    public void setMcDcDataColumnDescriptor(String name) {
        
        mcdcDataColumnDescriptor = name;
    }
    
    public void applySettings(AdvancedSettings settings){
        
        if(settings != null){
            setTestNotMandatory(settings.getTestNotMandatory());
            setClassDataColumnDescriptor(settings.getClassDataColumnDescriptor());
            setMethodDataColumnDescriptor(settings.getMethodDataColumnDescriptor());
            setBlockDataColumnDescriptor(settings.getBlockDataColumnDescriptor());
            setLineDataColumnDescriptor(settings.getLineDataColumnDescriptor());
            setConditionDataColumnDescriptor(settings.getConditionDataColumnDescriptor());
            setDecisionDataColumnDescriptor(settings.getDecisionDataColumnDescriptor());
            setMcDcDataColumnDescriptor(settings.getMcDcDataColumnDescriptor());
            setMccDataColumnDescriptor(settings.getMccDataColumnDescriptor());
        }
    }    
}
//
////////////////////////////////////////////////////////////////////////////////
