package mapgen;

import mapgen.blocks.BlockType;
import mapgen.graph.Edge;
import mapgen.graph.Graph;
import mapgen.graph.Vertex;

import java.util.ArrayList;

public class Generator {

    private final int gridWidth, gridHeight;
    private final int[][] grid;
    //public Difficulty main;
    private Graph graph;
    private final int border = 10;

    private int distX, distY, meshSize, minWidth, maxPlay, startX, startY, endX, endY;

    public Generator(int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        //main = new Difficulty(0.05, 1.0 / 30.0);

        grid = new int[gridHeight][gridWidth];
    }

    public void generateEmptyGrid() {
        for(int width = 0; width < gridWidth; width++) {
            for(int height = 0; height < gridHeight; height++) {
                setBlockType(width, height, BlockType.HOOKABLE);
            }
        }
    }

    public void generateFromGraph(int startX, int startY, int endX, int endY, int meshSize, int minWidth, int maxPlay) {
        distX = (gridWidth - 2*border) / (meshSize-1);
        distY = (gridHeight - 2*border) / (meshSize-1);

        this.meshSize = meshSize;
        this.minWidth = minWidth;
        this.maxPlay = maxPlay;
        this.startX = startX-1;
        this.startY = startY-1;
        this.endX = endX-1;
        this.endY = endY-1;

        generateGraph();
        connectGraph();
        addFreeze();

        generateSpawn(border+this.startX*distX, border+this.startY*distY, 10);
        generateFinish(border+this.endX*distX, border+this.endY*distY, 10);
    }

    public void generateSpawn(int x, int y, int size) {
        if(size <= 0) return;

        if(size % 2 == 0) size++;
        int dist = size/2;

        // Checking borders
        if(x <= dist || y <= dist) return;
        if(x >= gridWidth - dist || y >= gridHeight - dist) return;

        int startX = x - dist;
        int startY = y - dist;

        // Fill spawn area with start blocks
        for(int sY = startY - 1; sY < startY + size + 1; sY++) {
            for(int sX = startX-1; sX < startX + size + 1; sX++) {
                if(getBlockType(sX, sY) == BlockType.EMPTY
                        || getBlockType(sX, sY) == BlockType.FREEZE) {
                    setBlockType(sX, sY, BlockType.START);
                }
            }
        }

        // Generate empty spawn area
        for(int i = startX; i < startX + size; i++) {
            for(int k = startY; k < startY + size; k++) {
                setBlockType(i, k, BlockType.EMPTY);
            }
        }

        // Generate spawn with 3x1 platform below
        setBlockType(x, y, BlockType.SPAWN);
        for(int i = 0; i < 3; i++) {
            setBlockType(x - 1 + i, y + 1, BlockType.HOOKABLE);
        }
    }

    public void generateFinish(int x, int y, int size) {
        if(size <= 0) return;

        if(size % 2 == 0) size++;
        int dist = size/2;

        // Checking borders
        if(x <= dist || y <= dist) return;
        if(x >= gridWidth - dist || y >= gridHeight - dist) return;

        int startX = x - dist;
        int startY = y - dist;

        // Fill finish area with start blocks
        for(int sY = startY - 1; sY < startY + size + 1; sY++) {
            for(int sX = startX-1; sX < startX + size + 1; sX++) {
                if(getBlockType(sX, sY) == BlockType.EMPTY
                        || getBlockType(sX, sY) == BlockType.FREEZE) {
                    setBlockType(sX, sY, BlockType.FINISH);
                }
            }
        }

        // Generate empty finish area
        for(int i = startX; i < startX + size; i++) {
            for(int k = startY; k < startY + size; k++) {
                setBlockType(i, k, BlockType.EMPTY);
            }
        }
    }

    public ArrayList<Integer> getSurroundingBlocks(int x, int y) {
        ArrayList<Integer> blocks = new ArrayList<>();

        blocks.add(getBlockType(x-1, y-1));
        blocks.add(getBlockType(x, y-1));
        blocks.add(getBlockType(x+1, y-1));
        blocks.add(getBlockType(x-1, y));
        blocks.add(getBlockType(x+1, y));
        blocks.add(getBlockType(x-1, y+1));
        blocks.add(getBlockType(x, y+1));
        blocks.add(getBlockType(x-1, y+1));

        return blocks;
    }

    public void addFreeze() {
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                if(getBlockType(x,y) == BlockType.EMPTY) {
                    ArrayList<Integer> surroundings = getSurroundingBlocks(x,y);
                    for(int i : surroundings) {
                        if(i == BlockType.HOOKABLE) setBlockType(x,y, BlockType.FREEZE);
                    }
                }
            }
        }
    }

    public void generateGraph() {
        graph = new Graph();

        for(int x = 0; x < meshSize; x++) {
            for(int y = 0; y < meshSize; y++) {
                Vertex vertex = new Vertex(border+distX*x, border+distY*y);
                graph.addVertex(vertex);
            }
        }
    }

    public void connectGraph() {

        graph.setAllVisited(false);
        connectGraph(startX,startY);

        paintTrimmedGraph(graph.vertexAt(border+startX*distX, border+startY*distY),
                graph.vertexAt(border+endX*distX, border+endY*distY));

    }

    private void connectGraph(int x, int y) {
        int vertexX = border+x*distX;
        int vertexY = border+y*distY;

        Vertex current = graph.vertexAt(vertexX,vertexY);

        if(current == null) return;

        current.setVisited(true);

        // Check termination conditions
        if(!hasUnvisitedNeighbours(x,y)) return;

        // Randomly decide whether to move in x or y direction
        boolean moveX = false;
        double dir = Math.random();
        if(dir <= 0.5) moveX = true;

        int randomX = 0, randomY = 0;

        if(moveX) {
            if(x == 0) randomX = 1;
            else if(x == meshSize-1) randomX = -1;
            else randomX = Math.random() <= 0.5 ? -1 : 1;

            int toX = border+(x+randomX)*distX;
            int toY = border+(y+randomY)*distY;
            Vertex to = graph.vertexAt(toX, toY);

            if(!to.isVisited()) {
                Edge edge = new Edge(current, to);
                graph.addEdge(edge);
                //paintEdge(edge, false);
            }
            else connectGraph(x,y);
        } else {
            if(y == 0) randomY = 1;
            else if(y == meshSize-1) randomY = -1;
            else randomY = Math.random() <= 0.5 ? -1 : 1;

            int toX = border+(x+randomX)*distX;
            int toY = border+(y+randomY)*distY;
            Vertex to = graph.vertexAt(toX, toY);

            if(!to.isVisited()) {
                Edge edge = new Edge(current, to);
                graph.addEdge(edge);
                //paintEdge(edge, true);
            }
            else connectGraph(x,y);
        }

        connectGraph(x+randomX, y+randomY);
    }

    public void paintTrimmedGraph(Vertex from, Vertex to) {
        ArrayList<Edge> edges = graph.dfs(from, to);

        for(Edge e : edges) {
            paintEdge(e);
        }
    }

    private void paintEdge(Edge edge) {
        if(edge == null) return;

        Vertex from = edge.getFrom();
        Vertex to = edge.getTo();

        int fromX = from.getX();
        int fromY = from.getY();
        int toX = to.getX();
        int toY = to.getY();

        if(toX < fromX) {
            fromX = to.getX();
            toX = from.getX();
        }
        if(toY < fromY) {
            fromY = to.getY();
            toY = from.getY();
        }

        // vertical line
        if(fromX == toX) {
            for(int y = fromY-2 ; y < toY+2; y++) {
                int leftPlay = (int)(Math.random() * maxPlay);
                int rightPlay = (int)(Math.random() * maxPlay);

                int leftX = fromX - minWidth - leftPlay;
                int rightX = fromX + minWidth + rightPlay;

                for(int x = leftX; x < rightX; x++) {
                    setBlockType(x, y, BlockType.EMPTY);
                }
            }
        // horizontal line
        } else {
            for(int x = fromX-2; x < toX+2; x++) {
                int upperPlay = (int)Math.round(Math.random() * maxPlay);
                int lowerPlay = (int)Math.round(Math.random() * maxPlay);

                int upperY = fromY - minWidth - upperPlay;
                int lowerY = fromY + minWidth + lowerPlay;

                for(int y = upperY; y < lowerY; y++) {
                    setBlockType(x, y, BlockType.EMPTY);
                }
            }
        }

    }

    private boolean hasUnvisitedNeighbours(int meshX, int meshY) {
        int unvisited = 0;

        int x = border+meshX*distX;
        int y = border+meshY*distY;

        if(meshX > 0)
            unvisited = !graph.vertexAt(border+(meshX-1)*distX,y).isVisited() ? unvisited+1 : unvisited;
        if(meshX < meshSize-1)
            unvisited = !graph.vertexAt(border+(meshX+1)*distX,y).isVisited() ? unvisited+1 : unvisited;
        if(meshY > 0)
            unvisited = !graph.vertexAt(x,border+(meshY-1)*distY).isVisited() ? unvisited+1 : unvisited;
        if(meshY < meshSize-1)
            unvisited = !graph.vertexAt(x,border+(meshY+1)*distY).isVisited() ? unvisited+1 : unvisited;

        return unvisited > 0;
    }

    public void floodCenter() {
        int centerX = gridWidth / 2;
        int centerY = gridHeight / 2;

        boolean isEmpty = false;

        int h = 1;

        while(!isEmpty) {
            if(getBlockType(centerX, centerY) == BlockType.EMPTY) {
                isEmpty = true;
            } else centerX += h;
            if(getBlockType(centerX, centerY) == BlockType.EMPTY) {
                isEmpty = true;
            } else centerY += h;
            h++;
        }

        floodCenter(centerX, centerY);
    }

    private void floodCenter(int x, int y) {
        setBlockType(x,y, BlockType.FLOOD);

        if(getBlockType(x-1, y) == BlockType.EMPTY) {
            floodCenter(x-1, y);
        }
        if(getBlockType(x+1, y) == BlockType.EMPTY) {
            floodCenter(x+1, y);
        }
        if(getBlockType(x, y-1) == BlockType.EMPTY) {
            floodCenter(x, y-1);
        }
        if(getBlockType(x, y+1) == BlockType.EMPTY) {
            floodCenter(x, y+1);
        }
    }

    public void removeUnflooded() {
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                if(getBlockType(x,y) == BlockType.EMPTY) {
                    setBlockType(x,y, BlockType.HOOKABLE);
                }
            }
        }
    }

    public void unflood() {
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                if(getBlockType(x,y) == BlockType.FLOOD) {
                    setBlockType(x,y, BlockType.EMPTY);
                }
            }
        }
    }

    public void generateNoiseMap(long seed, double lim, double frequency) {
        for(int x = 5; x < gridWidth - 5; x++) {
            for(int y = 5; y < gridHeight - 5; y++) {
                double value = OpenSimplex2S.noise2(seed, x*frequency, y*frequency);
                if(value <= lim) {
                    setBlockType(x, y, BlockType.EMPTY);
                }
            }
        }
    }

    public void setBlockType(int x, int y, int blockType) {
        grid[y][x] = blockType;
    }

    public int getBlockType(int x, int y) {
        return grid[y][x];
    }

    public int[][] getGrid() {
        return grid;
    }

}
