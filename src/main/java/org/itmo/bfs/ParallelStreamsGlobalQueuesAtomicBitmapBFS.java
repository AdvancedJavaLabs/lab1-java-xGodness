package org.itmo.bfs;

import lombok.EqualsAndHashCode;
import org.itmo.AtomicBitmap;
import org.itmo.Graph;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@EqualsAndHashCode
public class ParallelStreamsGlobalQueuesAtomicBitmapBFS implements BreadthFirstSearch {

    @Override
    public boolean[] execute(Graph graph) {
        int vertexCount = graph.getVertexCount();
        if (vertexCount == 0) {
            throw new IllegalArgumentException("Empty graph cannot be traversed");
        }

        List<Integer>[] edges = graph.getEdges();

        AtomicBitmap visitedBitmap = new AtomicBitmap(vertexCount);

        Queue<Integer> firstQueue = new ConcurrentLinkedQueue<>();
        Queue<Integer> secondQueue = new ConcurrentLinkedQueue<>();
        AtomicBoolean useFirstAsCur = new AtomicBoolean(true);

        firstQueue.add(0);

        Queue<Integer> curQueue = firstQueue;
        Queue<Integer> nextQueue = secondQueue;

        while (!curQueue.isEmpty()) {
            final Queue<Integer> nq = nextQueue;
            curQueue.parallelStream().forEach(v ->
                    edges[v].parallelStream().forEach(next -> {
                        if (visitedBitmap.setBitCAS(next)) {
                            nq.add(next);
                        }
                    }));
            curQueue.clear();

            if (useFirstAsCur.get()) {
                useFirstAsCur.set(false);
                curQueue = secondQueue;
                nextQueue = firstQueue;
            } else {
                useFirstAsCur.set(true);
                curQueue = firstQueue;
                nextQueue = secondQueue;
            }
        }

        return visitedBitmap.toBooleanArray();
    }

    @Override
    public String getDescription() {
        return "Parallel streams + Global queues + AtomicBitmap";
    }
}
