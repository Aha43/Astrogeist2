package astrogeist.app;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public final class Main {

    public static void main(String[] args) {
        boolean demo = false;
        for (String arg : args) if ("--demo".equals(arg)) { demo = true; break; }
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", AppInfo.NAME);
        FlatDarkLaf.setup();
        final boolean demoMode = demo;
        SwingUtilities.invokeLater(() -> App.launch(demoMode));
    }
}
