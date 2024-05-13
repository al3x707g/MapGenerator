package mapgen.gui;

import mapgen.blocks.BlockColor;
import mapgen.Generator;

import javax.swing.*;
import java.awt.*;

public class Panel extends JPanel {

    private final int width, height, gridWidth, gridHeight;
    private final Generator gen;

    public Panel(int width, int height, int gridWidth, int gridHeight) {
        this.width = width;
        this.height = height;
        init();

        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        gen = new Generator(gridWidth, gridHeight);
        gen.generateEmptyGrid();

        gen.generateFromGraph(1, 1, 30, 30, 30, 3, 1);

        //gen.outputMap();

        /*gen.generateNoiseMap(123456789, gen.main.lim(), gen.main.frequency());
        gen.floodCenter();
        gen.removeUnflooded();
        gen.unflood();*/
    }

    private void init() {
        setSize(new Dimension(width, height));

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder());

        setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int blockSize = width / gridWidth;

        for(int w = 0; w < gridWidth; w++) {
            for(int h = 0; h < gridHeight; h++) {
                int blockType = gen.getBlockType(w, h);
                Color color = BlockColor.getColor(blockType);
                g2d.setColor(color);
                g2d.fillRect(w*blockSize, h*blockSize, blockSize, blockSize);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(w*blockSize, h*blockSize, blockSize, blockSize);
            }
        }
    }

}
