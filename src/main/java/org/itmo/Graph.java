package org.itmo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class Graph {
    private final int vertexCount;
    private final List<Integer>[] edges;

    public Graph(int vertexCount) {
        this.vertexCount = vertexCount;
        edges = new ArrayList[vertexCount];
        for (int i = 0; i < vertexCount; ++i) {
            edges[i] = new ArrayList<>();
        }
    }

    public void addEdge(int src, int dest) {
        if (!edges[src].contains(dest)) {
            edges[src].add(dest);
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public List<Integer>[] getEdges() {
        return edges;
    }
}
