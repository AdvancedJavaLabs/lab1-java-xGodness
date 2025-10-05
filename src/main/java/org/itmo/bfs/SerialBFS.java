package org.itmo.bfs;

import lombok.EqualsAndHashCode;
import org.itmo.Graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@EqualsAndHashCode
public class SerialBFS implements BreadthFirstSearch {

    @Override
    public boolean[] execute(Graph graph) {
        int vertexCount = graph.getVertexCount();
        if (vertexCount == 0) {
            throw new IllegalArgumentException("Empty graph cannot be traversed");
        }

        boolean[] visited = new boolean[vertexCount];
        List<Integer>[] edges = graph.getEdges();

        Queue<Integer> queue = new LinkedList<>();
        queue.add(0);
        visited[0] = true;

        int vertex;
        while (!queue.isEmpty()) {
            vertex = queue.poll();
            for (int next : edges[vertex]) {
                if (visited[next]) {
                    continue;
                }
                queue.add(next);
                visited[next] = true;
            }
        }
        return visited;
    }

    @Override
    public String getDescription() {
        return "Serial";
    }
}
