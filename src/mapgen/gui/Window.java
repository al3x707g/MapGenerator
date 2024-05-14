package mapgen.gui;

import mapgen.MapLength;
import mapgen.Preset;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    private int width, height;

    private Preset easy,main,hard;

    private JPanel panel;

    public Window() {
        init();
        createWindow();
    }


    private void init() {

        int length = MapLength.MEDIUM;
        easy = new Preset(MapLength.MEDIUM, 1, 1, (int)(length*0.04), (int)(length*0.04), (int)(length*0.04), 4, 4);
        main = new Preset(MapLength.MEDIUM, 1, 1, (int)(length*0.075), (int)(length*0.075), (int)(length*0.075), 2, 3);
        hard = new Preset(MapLength.MEDIUM, 1, 1, (int)(length*0.1), (int)(length*0.1), (int)(length*0.1), 1, 3);
    }

    private void createWindow() {

        //setSize(new Dimension(width, height));

        createPanel();
        add(panel);

        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //setVisible(true);

    }

    private void createPanel() {
        this.panel = new Panel(main);
    }

}