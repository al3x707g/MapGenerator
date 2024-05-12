package mapgen.graph;

public class Vertex {

    private final int x,y;
    private boolean visited;

    public Vertex(int x, int y) {
        this.x = x;
        this.y = y;
        visited = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
