package org.itmo.bfs;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.itmo.AtomicBitmap;
import org.itmo.Graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Slf4j
@EqualsAndHashCode
public class ThreadPoolLocalBuffersAtomicBitmapBFS implements BreadthFirstSearch {
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

        List<Integer> curList = new ArrayList<>();
        curList.add(0);

        try (ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_COUNT)) {
            int taskBatchSize;

            List<Callable<List<Integer>>> tasks;
            while (!curList.isEmpty()) {
                tasks = new LinkedList<>();
                taskBatchSize = Math.max(1, curList.size() / MAX_THREAD_COUNT);

                for (int i = 0; i < curList.size(); i += taskBatchSize) {
                    List<Integer> taskBatch = curList.subList(i, Math.min(i + taskBatchSize, curList.size()));
                    List<Integer> nextBatch = new ArrayList<>();
                    tasks.add(() -> {
                        for (int vertex : taskBatch) {
                            for (int next : edges[vertex]) {
                                if (visitedBitmap.setBitCAS(next)) {
                                    nextBatch.add(next);
                                }
                            }
                        }
                        return nextBatch;
                    });
                }

                curList = pool.invokeAll(tasks).stream()
                        .flatMap(future -> {
                            try {
                                return future.get().stream();
                            } catch (ExecutionException | InterruptedException ex) {
                                log.error(ex.getMessage());
                                Thread.currentThread().interrupt();
                            }
                            return Stream.empty();
                        }).toList();
            }
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }

        return visitedBitmap.toBooleanArray();
    }

    @Override
    public String getDescription() {
        return "Thread pool + Local buffers + AtomicBitmap";
    }
}
