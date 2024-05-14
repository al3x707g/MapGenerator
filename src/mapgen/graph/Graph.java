package mapgen.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Graph {

    private final List<Vertex> vertices;
    private final List<Edge> edges;

    public Graph() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public void addVertex(Vertex vertex) {
        if(vertices.contains(vertex)) return;

        if(vertexAt(vertex.getX(), vertex.getY()) == null)
            vertices.add(vertex);
    }

    public void addEdge(Edge edge) {
        if(edges.contains(edge)) return;

        edges.add(edge);

        if(!vertices.contains(edge.getFrom()))
            vertices.add(edge.getFrom());
        if(!vertices.contains(edge.getTo()))
            vertices.add(edge.getTo());
    }

    public ArrayList<Vertex> findNeighbours(Vertex vertex) {
        ArrayList<Vertex> result = new ArrayList<>();

        for(Edge e : getEdges()) {
            if(e.getFrom() == vertex) result.add(e.getTo());
            if(e.getTo() == vertex) result.add(e.getFrom());
        }

        return result;
    }

    public boolean hasNeighbours(Vertex vertex) {
        for(Edge e : getEdges()) {
            if(e.getFrom() == vertex || e.getTo() == vertex) return true;
        }
        return false;
    }

    public boolean hasUnvisitedNeighbours(Vertex vertex) {
        ArrayList<Vertex> neighbours = findNeighbours(vertex);

        for(Vertex v : neighbours) {
            if(!v.isVisited()) return true;
        }

        return false;
    }

    public boolean allVisited() {
        for(Vertex v : getVertices()) {
            if(!v.isVisited()) return false;
        }
        return true;
    }

    public ArrayList<Edge> dfs(Vertex from, Vertex to) {
        if(from == null || to == null) return null;

        setAllVisited(false);

        ArrayList<Edge> result = new ArrayList<>();

        Stack<Vertex> stack = new Stack<>();
        stack.push(from);
        from.setVisited(true);

        Vertex current = from;
        ArrayList<Vertex> neighbours;
        Vertex next = null;

        while(current != to && !stack.isEmpty()) {
            current.setVisited(true);
            if(hasUnvisitedNeighbours(current)) {
                neighbours = findNeighbours(current);

                for (Vertex neighbour : neighbours) {
                    if (!neighbour.isVisited())
                        next = neighbour;
                }

                stack.push(next);
                current = next;
            } else {
                stack.pop();
                if(!stack.isEmpty())
                    current = stack.peek();
            }
        }

        while(!stack.isEmpty()) {
            Vertex edgeFrom = stack.peek();
            stack.pop();
            Vertex edgeTo = null;
            if(!stack.isEmpty())  edgeTo = stack.peek();

            if(edgeTo != null)
                result.add(findEdge(edgeFrom, edgeTo));
        }

        return result;
    }

    public Edge findEdge(Vertex from, Vertex to) {
        for(Edge e : edges) {
            if(e.getFrom() == from && e.getTo() == to)  return e;
            if(e.getTo() == from && e.getFrom() == to) return e;
            }

        return null;
    }

    public Vertex vertexAt(int x, int y) {
        for(Vertex v : vertices) {
            if(v.getX() == x && v.getY() == y) return v;
        }

        return null;
    }

    public void setAllVisited(boolean visited) {
        for(Vertex v : vertices) {
            v.setVisited(visited);
        }
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

}