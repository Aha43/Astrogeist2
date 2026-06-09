package astrogeist.scanner;

@FunctionalInterface
public interface ScanProgressListener {
    void onProgress(int done, int total, String folderName);
}
