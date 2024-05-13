package mapgen;

import mapgen.blocks.BlockColor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class MapOutput {

    public static String gridToString(int[][] grid) {
        String output = "";

        for(int y = 0; y < grid[0].length; y++) {
            for(int x = 0; x < grid.length; x++) {
                output += grid[y][x] + " ";
            }
            output += System.lineSeparator();
        }

        return output;
    }

    public static File gridToImage(int[][] grid) {
        File output = null;
        try {
            BufferedImage image = new BufferedImage(grid.length, grid[0].length, BufferedImage.TYPE_INT_BGR);
            for(int x = 0; x < grid.length; x++) {
                for(int y = 0; y < grid[0].length; y++) {
                    int block = grid[x][y];
                    Color clr = BlockColor.getColor(block);
                    image.setRGB(x,y,clr.getRGB());
                }
            }
            output = new File("map.png");
            ImageIO.write(image, "png", output);
        }

        catch(Exception e) {
            e.printStackTrace();
        }
        return output;
    }

}
