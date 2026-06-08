package astrogeist.app;

import javax.swing.*;
import java.awt.*;

public final class App {

    private App() {}

    public static void launch() {
        var frame = new JFrame(AppInfo.NAME + " " + AppInfo.version());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
