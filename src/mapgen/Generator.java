package mapgen;

import mapgen.blocks.BlockType;
import mapgen.graph.Edge;
import mapgen.graph.Graph;
import mapgen.graph.Vertex;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

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

    public void generateFromGraph(Preset preset) {
        this.meshSize = preset.meshSize();
        this.minWidth = preset.minWidth();
        this.maxPlay = preset.maxPlay();
        this.startX = preset.startX()-1;
        this.startY = preset.startY()-1;
        this.endX = preset.endX()-1;
        this.endY = preset.endY()-1;

        distX = (gridWidth - 2*border) / (meshSize-1);
        distY = (gridHeight - 2*border) / (meshSize-1);

        generateGraph();
        addHorizontalConstraint(border, border+3*(meshSize/4)*distX, border+(meshSize/4)*distY);
        addHorizontalConstraint(border+(meshSize/4)*distX, border+meshSize*distX, border+3*(meshSize/4)*distY);
        connectGraph();
        roundCorners();
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

    public void roundCorners() {
        for(int x = border; x < gridWidth-border; x++) {
            for(int y = border; y < gridHeight-border; y++) {
                if(getBlockType(x,y) == BlockType.HOOKABLE) {
                    ArrayList<Integer> surroundings = getSurroundingBlocks(x,y);
                    int countHookables = 0;
                    for(int block : surroundings) {
                        if(block == BlockType.HOOKABLE) countHookables++;
                    }
                    if(countHookables == 3) setBlockType(x,y,BlockType.EMPTY);
                }
            }
        }
    }

    public void outputImage() {
        File output = MapOutput.gridToImage(grid);
        System.out.println("Saved at '" +output.getAbsolutePath() + "'");
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
        blocks.add(getBlockType(x+1, y+1));

        return blocks;
    }

    public void addFreeze() {
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                if(getBlockType(x,y) == BlockType.EMPTY) {
                    ArrayList<Integer> surroundings = getSurroundingBlocks(x,y);
                    if(surroundings.contains(BlockType.HOOKABLE))
                        setBlockType(x,y, BlockType.FREEZE);
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

        int vertexX = border+startX*distX;
        int vertexY = border+startY*distY;

        Vertex start = graph.vertexAt(vertexX, vertexY);
        connectGraphIterative(start);

        Vertex end = graph.vertexAt(border+endX*distX, border+endY*distY);

        paintTrimmedGraph(start, end);

    }


    private void connectGraphRecursive(Vertex vertex) {
        if(vertex == null) return;
        vertex.setVisited(true);

        ArrayList<Vertex> neighbours = getUnvisitedNeighbours(vertex);
        if(neighbours.isEmpty()) return;

        int random = (int)Math.round(Math.random() * (neighbours.size()-1));

        Vertex next = neighbours.get(random);

        Edge edge = new Edge(vertex, next);
        graph.addEdge(edge);

        connectGraphRecursive(next);

        if(getUnvisitedNeighbours(next).isEmpty())
            connectGraphRecursive(vertex);
    }

    private void connectGraphIterative(Vertex vertex) {
        Stack<Vertex> vertices = new Stack<>();

        Vertex current = vertex;

        vertices.push(current);

        while(!vertices.isEmpty()) {
            if (current == null) return;

            current.setVisited(true);

            ArrayList<Vertex> neighbours = getUnvisitedNeighbours(current);
            if (!neighbours.isEmpty()) {

                int random = (int)(Math.random() * (neighbours.size()));

                Vertex next = neighbours.get(random);

                Edge edge = new Edge(current, next);
                graph.addEdge(edge);

                current = next;
                vertices.push(current);
            } else {
                vertices.pop();
                if(!vertices.isEmpty()) current = vertices.peek();
            }
        }
    }

    public void addHorizontalConstraint(int fromX, int toX, int y) {
        for(int i = fromX; i < toX; i++) {
            // Check whether the position (i,y) is a valid position in the mesh
            if((i-border) % distX == 0) {
                Vertex current = graph.vertexAt(i, y);

                if (current != null) graph.removeVertex(current);
            }
            // ONLY FOR VISUALISATION
            setBlockType(i,y,BlockType.FLOOD);
        }
    }

    public void paintTrimmedGraph(Vertex from, Vertex to) {
        ArrayList<Edge> edges = graph.dfs(from, to);

        int play = (int)(Math.random()*(maxPlay+1));

        for(Edge e : edges) {
            //Calculate new play based on previous play
            if(play == 0) play++;
            else if(play == maxPlay) play--;
            else play += (int)(Math.random()*maxPlay-1.99);

            paintEdge(e, play);
        }
    }

    private void paintEdge(Edge edge, int play) {
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
            int leftX = fromX - minWidth - play;
            int rightX = fromX + minWidth + play;

            for(int y = fromY ; y < toY; y++) {
                for(int x = leftX; x < rightX; x++) {
                    setBlockType(x, y, BlockType.EMPTY);
                }
            }
        // horizontal line
        } else {
            int upperY = fromY - minWidth - play;
            int lowerY = fromY + minWidth + play;

            for(int x = fromX; x < toX; x++) {
                for(int y = upperY; y < lowerY; y++) {
                    setBlockType(x, y, BlockType.EMPTY);
                }
            }
        }

    }

    private ArrayList<Vertex> getUnvisitedNeighbours(Vertex vertex) {
        ArrayList<Vertex> result = new ArrayList<>();

        int x = vertex.getX();
        int y = vertex.getY();

        if(x-distX >= 10) {
            Vertex neighbour = graph.vertexAt(x-distX, y);
            if(neighbour != null && !neighbour.isVisited()) result.add(neighbour);
        }
        if(x+distX < border+meshSize*distX) {
            Vertex neighbour = graph.vertexAt(x+distX, y);
            if(neighbour != null && !neighbour.isVisited()) result.add(neighbour);
        }
        if(y-distY >= 10) {
            Vertex neighbour = graph.vertexAt(x, y-distY);
            if(neighbour != null && !neighbour.isVisited()) result.add(neighbour);
        }
        if(y+distY < border+meshSize*distY) {
            Vertex neighbour = graph.vertexAt(x, y+distY);
            if(neighbour != null && !neighbour.isVisited()) result.add(neighbour);
        }

        return result;
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

    // Functional, but not producing playable map
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

    public void outputMap() {
        System.out.println(MapOutput.gridToString(grid));
    }

    public void setBlockType(int x, int y, int blockType) {
        grid[x][y] = blockType;
    }

    public int getBlockType(int x, int y) {
        return grid[x][y];
    }

    public int[][] getGrid() {
        return grid;
    }

}
