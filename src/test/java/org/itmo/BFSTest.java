package org.itmo;

import lombok.extern.slf4j.Slf4j;
import org.itmo.bfs.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class BFSTest {
    private static final int ITERATION_COUNT = 1;
    private static final String RESULTS_OUTPUT_PATH = "tmp/results.txt";

    private static RandomGraphGenerator randomGraphGenerator;
    private static FileWriter writer;
    private static Random random;

    @BeforeAll
    static void init() throws IOException {
        randomGraphGenerator = new RandomGraphGenerator();
        writer = new FileWriter(RESULTS_OUTPUT_PATH);
        random = new Random(42);
    }

    @Test
    void bfsTest() throws IOException {
        int[] sizes = new int[]{10, 100, 1000, 10_000, 10_000, 50_000, 100_000, 1_000_000, 2_000_000};
        int[] connections = new int[]{50, 500, 5000, 50_000, 100_000, 1_000_000, 1_000_000, 10_000_000, 14_000_000};
        Graph graph;

        List<BreadthFirstSearch> strategies = new LinkedList<>();
        strategies.add(new SerialBFS());
        strategies.add(new ParallelStreamsGlobalQueuesAtomicBooleanBFS());
        strategies.add(new ParallelStreamsGlobalQueuesAtomicBitmapBFS());
        strategies.add(new ThreadPoolGlobalQueuesAtomicBooleanBFS());
        strategies.add(new ThreadPoolGlobalQueuesAtomicBitmapBFS());
        strategies.add(new ThreadPoolLocalBuffersAtomicBooleanBFS());
        strategies.add(new ThreadPoolLocalBuffersAtomicBitmapBFS());

        Map<BreadthFirstSearch, List<Long>> map;
        Map<Integer, Map<BreadthFirstSearch, List<Long>>> statistics = new HashMap<>();
        for (int size : sizes) {
            map = new HashMap<>();
            for (BreadthFirstSearch strategy : strategies) {
                map.put(strategy, new ArrayList<>());
            }
            statistics.put(size, map);
        }

        long executionTime;
        for (int iter = 1; iter <= ITERATION_COUNT; iter++) {
            log.info("Staring iteration {}...", iter);

            for (int i = 0; i < sizes.length; i++) {
                log.info("Generating graph of size {} ...wait", sizes[i]);
                graph = randomGraphGenerator.generateGraph(random, sizes[i], connections[i]);
                log.info("Generation completed!");

                log.info("Executing BFS strategies...");
                for (BreadthFirstSearch strategy : strategies) {
                    executionTime = executeBFSAndGetTime(graph, strategy);
                    statistics.get(sizes[i]).get(strategy).add(executionTime);
                }
            }
        }

        StringJoiner sj = new StringJoiner("\n");
        sj.add("AVERAGE RESULTS ON %d ITERATION(S)".formatted(ITERATION_COUNT));
        sj.add("-".repeat(60) + "+" + "-".repeat(9));
        List<Long> times;
        double avg;
        for (int i = 0; i < sizes.length; i++) {
            sj.add("%-60s".formatted("|V| = %d, |E| = %d:".formatted(sizes[i], connections[i])) + "|");
            map = statistics.get(sizes[i]);
            for (BreadthFirstSearch strategy : strategies) {
                times = map.get(strategy);
                avg = times.stream().mapToLong(Long::longValue).average().orElse(-1);
                sj.add("%-60s| %.2f".formatted(strategy.getDescription(), avg));
            }
            sj.add("-".repeat(60) + "+" + "-".repeat(9));
        }

        writer.append(sj.toString());
        writer.flush();
    }

    private long executeBFSAndGetTime(Graph graph, BreadthFirstSearch bfs) {
        long startTime = System.currentTimeMillis();
        boolean[] visited = bfs.execute(graph);
        long endTime = System.currentTimeMillis();
        for (boolean v : visited) {
            assertTrue(v);
        }
        return endTime - startTime;
    }

    @AfterAll
    static void shutdown() throws IOException {
        writer.close();
    }

}
