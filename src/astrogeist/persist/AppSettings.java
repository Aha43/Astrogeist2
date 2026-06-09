package astrogeist.persist;

public final class AppSettings {

    private String lastFolder      = "";
    private String lastScanner     = "SharpCap";
    private String dataRootFolder  = "";
    private int[]  columnWidths    = new int[0];
    private boolean denseMode      = false;

    public String  getLastFolder()     { return lastFolder; }
    public String  getLastScanner()    { return lastScanner; }
    public String  getDataRootFolder() { return dataRootFolder; }
    public int[]   getColumnWidths()   { return columnWidths.clone(); }
    public boolean isDenseMode()       { return denseMode; }

    public void setLastFolder(String v)     { lastFolder     = v != null ? v : ""; }
    public void setLastScanner(String v)    { lastScanner    = v != null ? v : "SharpCap"; }
    public void setDataRootFolder(String v) { dataRootFolder = v != null ? v : ""; }
    public void setColumnWidths(int[] v)    { columnWidths   = v != null ? v.clone() : new int[0]; }
    public void setDenseMode(boolean v)     { denseMode      = v; }
}
