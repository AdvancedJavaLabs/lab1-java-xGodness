package org.itmo.bfs;

import lombok.EqualsAndHashCode;
import org.itmo.AtomicBitmap;
import org.itmo.Graph;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

@EqualsAndHashCode
public class ThreadPoolGlobalQueuesAtomicBitmapBFS implements BreadthFirstSearch {
    private final int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    @Override
    public boolean[] execute(Graph graph) {
        int vertexCount = graph.getVertexCount();
        if (vertexCount == 0) {
            throw new IllegalArgumentException("Empty graph cannot be traversed");
        }

        List<Integer>[] edges = graph.getEdges();

        AtomicBitmap visitedBitmap = new AtomicBitmap(vertexCount);
        visitedBitmap.setBit(0);

        Queue<Integer> firstQueue = new ConcurrentLinkedQueue<>();
        Queue<Integer> secondQueue = new ConcurrentLinkedQueue<>();
        AtomicBoolean useFirstAsCur = new AtomicBoolean(true);

        firstQueue.add(0);
        Phaser phaser = new Phaser(1);

        Runnable task = () -> {
            try {
                Queue<Integer> curQueue;
                Queue<Integer> nextQueue;
                if (useFirstAsCur.get()) {
                    curQueue = firstQueue;
                    nextQueue = secondQueue;
                } else {
                    curQueue = secondQueue;
                    nextQueue = firstQueue;
                }

                Integer vertex = curQueue.poll();
                while (vertex != null) {
                    for (int next : edges[vertex]) {
                        if (visitedBitmap.setBitCAS(next)) {
                            nextQueue.add(next);
                        }
                    }
                    vertex = curQueue.poll();
                }
            } finally {
                phaser.arrive();
            }
        };

        try (ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_COUNT)) {
            phaser.bulkRegister(MAX_THREAD_COUNT);
            Queue<Integer> curQueue = firstQueue;

            while (!curQueue.isEmpty()) {
                for (int i = 0; i < MAX_THREAD_COUNT; i++) {
                    pool.submit(task);
                }

                int phase = phaser.arrive();
                phaser.awaitAdvance(phase);

                if (useFirstAsCur.get()) {
                    useFirstAsCur.set(false);
                    curQueue = secondQueue;
                } else {
                    useFirstAsCur.set(true);
                    curQueue = firstQueue;
                }
            }
        }

        return visitedBitmap.toBooleanArray();
    }

    @Override
    public String getDescription() {
        return "Thread pool + Global queues + AtomicBitmap";
    }
}
