package hudson.plugins.cocoemma;


/**
 * @author Kohsuke Kawaguchi
 */
public class EmmaBuildActionTest extends AbstractEmmaTestBase {
  
    public void testLoadCocoReport1() throws Exception {
        EmmaBuildAction r = EmmaBuildAction.load(null,null,
                new EmmaHealthReportThresholds(30, 90, 25, 80, 20, 70, 15, 60,20,70,21,71,22,72,23,73),
                getClass().getResourceAsStream("coco1.xml"));

        assertRatio(r.clazz, 75,94);
        assertRatio(r.method, 947,2176);
        assertRatio(r.block, 2842,7967);
        assert(r.line == null);
        assertRatio(r.condition, 5917,15253);
 
        String description = r.getBuildHealth().getDescription() ;
        assertEquals("Coverage: Class 75/94 (80%). Function 947/2176 (44%). Statement (Block) 2842/7967 (36%). Condition 5917/15253 (39%).",
                     description);
    }

    public void testLoadCocoReport2() throws Exception {
        EmmaBuildAction r = EmmaBuildAction.load(null,null,
                new EmmaHealthReportThresholds(30, 90, 25, 80, 20, 70, 15, 60,20,70,21,71,22,72,23,73),
                getClass().getResourceAsStream("coco2.xml"));

        assertRatio(r.clazz, 1,1);
        assertRatio(r.method, 1,1);
        assertRatio(r.block, 2,5);
        assertRatio(r.line, 8,13);
        assertRatio(r.condition, 4,15);
 
        String description = r.getBuildHealth().getDescription() ;
        assertEquals("Coverage: Statement (Block) 2/5 (40%). Condition 4/15 (27%).",
                     description);
    }
    
    
    public void testLoadCocoReport3() throws Exception {
        EmmaBuildAction r = EmmaBuildAction.load(null,null,
                new EmmaHealthReportThresholds(30, 90, 25, 80, 20, 70, 15, 60,20,70, 21,71,22,72,23,73),
                getClass().getResourceAsStream("coco3.xml"));

        assertRatio(r.clazz, 1,1);
        assertRatio(r.method, 1,1);
        assertRatio(r.block, 2,5);
        assert(r.line==null);
        assert(r.condition==null);
 
        String description = r.getBuildHealth().getDescription() ;
        assertEquals("Coverage: Statement (Block) 2/5 (40%).",
                     description);
    }
    
    public void testLoadReport1() throws Exception {
        EmmaBuildAction r = EmmaBuildAction.load(null,null,
                new EmmaHealthReportThresholds(30, 90, 25, 80, 20, 70, 15, 60,20,70,21,71,22,72,23,73),
                getClass().getResourceAsStream("coverage.xml"));
        assertEquals(100, r.clazz.getPercentage(r.getTestNotMandatory()));
        assertEquals(64, r.line.getPercentage(r.getTestNotMandatory()));
        assertRatio(r.clazz, 185,185);
        assertRatio(r.method, 1345,2061);
        assertRatio(r.block, 44997,74846);
        assertRatio(r.line, 8346.3f,13135);
        assert(r.condition == null);
        assertEquals("Coverage: Function 1345/2061 (65%). Statement (Block) 44997/74846 (60%).",
                     r.getBuildHealth().getDescription());
    }
    
    public void testLoadReport2() throws Exception {
        EmmaBuildAction r = EmmaBuildAction.load(null,null,
                new EmmaHealthReportThresholds(30, 90, 25, 80, 20, 70, 15, 60,20,70,21,71,22,72,23,73),
                getClass().getResourceAsStream("coverageh.xml"));
        assertEquals(1, r.clazz.getPercentage(r.getTestNotMandatory()));
        assertEquals(1, r.line.getPercentage(r.getTestNotMandatory()));
        assertRatio(r.clazz, 1, 149);
        assertRatio(r.method, 2, 678);
        assertRatio(r.block, 42, 9659);
        assertRatio(r.line, 9, 1693);
        assert(r.condition == null);
        String description = r.getBuildHealth().getDescription() ;
        assertEquals("Coverage: Class 1/149 (1%). Function 2/678 (0%). Statement (Block) 42/9659 (0%). Line 9/1693 (1%).",
                     description);
    }
    
    public void testLoadMultipleReports() throws Exception {
      EmmaBuildAction r = EmmaBuildAction.load(null,null,
              new EmmaHealthReportThresholds(30, 90, 25, 80, 20, 70, 15, 60,20,70,21,71,22,72,23,73),
              getClass().getResourceAsStream("coverage.xml"), 
              getClass().getResourceAsStream("coverageh.xml"));
      assertEquals(56, r.clazz.getPercentage(r.getTestNotMandatory()));
      assertEquals(56, r.line.getPercentage(r.getTestNotMandatory()));
      assertRatio(r.clazz, 186, 334);
      assertRatio(r.method, 1347, 2739);
      assertRatio(r.block, 45039, 84505);
      assertRatio(r.line, 8355.3f,14828);
      assert(r.condition == null);
      String description = r.getBuildHealth().getDescription() ;
      assertEquals("Coverage: Class 186/334 (56%). Function 1347/2739 (49%). Statement (Block) 45039/84505 (53%). Line 8355.3/14828 (56%).",
                   description);
  }
}
