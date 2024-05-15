package mapgen.gui;

import mapgen.Preset;
import mapgen.blocks.BlockColor;
import mapgen.Generator;

import javax.swing.*;
import java.awt.*;

public class Panel extends JPanel {

    private final int width, height, gridWidth, gridHeight;
    private final Generator gen;

    public Panel(Preset preset) {
        this.width = 917;
        this.height = 940;
        init();

        this.gridWidth = this.gridHeight = preset.gridSize();

        gen = new Generator(gridWidth, gridHeight);
        gen.generateEmptyGrid();

        gen.generateFromGraph(preset);

        gen.outputImage();

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
