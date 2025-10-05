package org.itmo.bfs;

import org.itmo.Graph;

public interface BreadthFirstSearch {
    boolean[] execute(Graph graph);
    String getDescription();
}
