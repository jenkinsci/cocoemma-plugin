package hudson.plugins.cocoemma;

import java.io.Serializable;

/**
 * Holds the configuration details for {@link hudson.model.HealthReport} generation
 *
 * @author Stephen Connolly
 * @since 1.7
 */
public class EmmaHealthReportThresholds implements Serializable {
    private int minClass;
    private int maxClass;
    private int minMethod;
    private int maxMethod;
    private int minBlock;
    private int maxBlock;
    private int minLine;
    private int maxLine;
    private int minCondition;
    private int maxCondition;
    private int minDecision;
    private int maxDecision;
    private int minMcc;
    private int maxMcc;
    private int minMcDc;
    private int maxMcDc;

    public EmmaHealthReportThresholds() {
    }

    public EmmaHealthReportThresholds(
            int minClass, int maxClass, 
            int minMethod, int maxMethod, 
            int minBlock, int maxBlock, 
            int minLine, int maxLine, 
            int minDecision, int maxDecision,
            int minCondition, int maxCondition,
            int minMcDc, int maxMcDc,
            int minMcc, int maxMcc
            ) {
        this.minClass = minClass;
        this.maxClass = maxClass;
        this.minMethod = minMethod;
        this.maxMethod = maxMethod;
        this.minBlock = minBlock;
        this.maxBlock = maxBlock;
        this.minLine = minLine;
        this.maxLine = maxLine;
        this.minDecision = minDecision;
        this.maxDecision = maxDecision;
        this.minCondition = minCondition;
        this.maxCondition = maxCondition;
        this.minMcDc = minMcDc;
        this.maxMcDc = maxMcDc;
        this.minMcc = minMcc;
        this.maxMcc = maxMcc;
        ensureValid();
    }

    private int applyRange(int min , int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public void ensureValid() {
        maxClass = applyRange(0, maxClass, 100);
        minClass = applyRange(0, minClass, maxClass);
        maxMethod = applyRange(0, maxMethod, 100);
        minMethod = applyRange(0, minMethod, maxMethod);
        maxBlock = applyRange(0, maxBlock, 100);
        minBlock = applyRange(0, minBlock, maxBlock);
        maxLine = applyRange(0, maxLine, 100);
        minLine = applyRange(0, minLine, maxLine);
        maxCondition = applyRange(0, maxCondition, 100);
        minCondition = applyRange(0, minCondition, maxCondition);
        maxDecision = applyRange(0, maxDecision, 100);
        minDecision = applyRange(0, minDecision, maxDecision);
        maxMcc = applyRange(0, maxMcc, 100);
        minMcc = applyRange(0, minMcc, maxMcc);
        maxMcDc = applyRange(0, maxMcDc, 100);
        minMcDc = applyRange(0, minMcDc, maxMcDc);
    }

    public int getMinClass() {
        return minClass;
    }

    public void setMinClass(int minClass) {
        this.minClass = minClass;
    }

    public int getMaxClass() {
        return maxClass;
    }

    public void setMaxClass(int maxClass) {
        this.maxClass = maxClass;
    }

    public int getMinMethod() {
        return minMethod;
    }

    public void setMinMethod(int minMethod) {
        this.minMethod = minMethod;
    }

    public int getMaxMethod() {
        return maxMethod;
    }

    public void setMaxMethod(int maxMethod) {
        this.maxMethod = maxMethod;
    }

    public int getMinBlock() {
        return minBlock;
    }

    public void setMinBlock(int minBlock) {
        this.minBlock = minBlock;
    }

    public int getMaxBlock() {
        return maxBlock;
    }

    public void setMaxBlock(int maxBlock) {
        this.maxBlock = maxBlock;
    }

    public int getMinLine() {
        return minLine;
    }

    public void setMinLine(int minLine) {
        this.minLine = minLine;
    }

    public int getMaxLine() {
        return maxLine;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }
    
    public void setMinCondition(int minCondition) {
        this.minCondition = minCondition;
    }

    public int getMinCondition() {
        return minCondition;
    }

    public void setMaxCondition(int maxCondition) {
        this.maxCondition = maxCondition;
    }

    public int getMaxCondition() {
        return maxCondition;
    }

    
    public void setMinDecision(int v) {
        this.minDecision = v;
    }

    public int getMinDecision() {
        return minDecision;
    }

    public void setMaxDecision(int v) {
        this.maxDecision = v;
    }

    public int getMaxDecision() {
        return maxDecision;
    }

    public void setMinMcDc(int v) {
        this.minMcDc = v;
    }

    public int getMinMcDc() {
        return minMcDc;
    }

    public void setMaxMcDc(int v) {
        this.maxMcDc = v;
    }

    public int getMaxMcDc() {
        return maxMcDc;
    }

    public void setMinMcc(int v) {
        this.minMcc = v;
    }

    public int getMinMcc() {
        return minMcc;
    }

    public void setMaxMcc(int v) {
        this.maxMcc = v;
    }

    public int getMaxMcc() {
        return maxMcc;
    }

}
