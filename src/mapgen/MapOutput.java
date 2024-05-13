package mapgen;

public class MapOutput {

    private int[][] grid;

    public MapOutput(int[][] grid) {
        this.grid = grid;
    }

    public String gridToString() {
        String output = "";

        for(int y = 0; y < grid[0].length; y++) {
            for(int x = 0; x < grid.length; x++) {
                output += grid[y][x] + " ";
            }
            output += System.lineSeparator();
        }

        return output;
    }

}
