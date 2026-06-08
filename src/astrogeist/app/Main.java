package astrogeist.app;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public final class Main {

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", AppInfo.NAME);
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(App::launch);
    }
}
