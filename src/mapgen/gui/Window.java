package mapgen.gui;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    private int width, height;
    private JPanel panel;

    public Window() {
        init();
        createWindow();
    }


    private void init() {

        width = 917;
        height = 940;

    }

    private void createWindow() {

        setSize(new Dimension(width, height));

        createPanel();
        add(panel);

        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setVisible(true);

    }

    private void createPanel() {
        this.panel = new Panel(width, height);
    }

}