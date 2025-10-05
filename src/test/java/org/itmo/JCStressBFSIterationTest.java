package org.itmo;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.JJJJJJJJ_Result;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@JCStressTest
@State
@Outcome(
        id = "1048575, 0, 0, 0, 0, 0, 0, 0",
        expect = Expect.ACCEPTABLE,
        desc = "every vertex visited"
)
public class JCStressBFSIterationTest {
    private static final int vertexCount = 20;
    private static final List<Integer>[] edges;
    private static final AtomicBitmap visitedBitmap;
    private static final Queue<Integer> queue;

    static {
        if (vertexCount > 512) {
            throw new IllegalArgumentException("Max vertex count for JCStress tests is 512");
        }

        visitedBitmap = new AtomicBitmap(vertexCount);

        RandomGraphGenerator graphGenerator = new RandomGraphGenerator();
        int edgeCount = new Random().nextInt(
                (int) ((vertexCount - 1) + 0.1 * (vertexCount * (vertexCount - 1)) / 2),
                (vertexCount * (vertexCount - 1)) / 2 + 1
        );
        Graph graph = graphGenerator.generateGraph(new Random(), vertexCount, edgeCount);
        edges = graph.getEdges();

        List<Integer> toVisit = IntStream.range(0, vertexCount)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(toVisit);

        queue = new ConcurrentLinkedQueue<>(toVisit);
    }

    @Actor
    public void actor1() {
        new BFSTask().run();
    }

    @Actor
    public void actor2() {
        new BFSTask().run();
    }

    @Arbiter
    public void arbiter(JJJJJJJJ_Result result) {
        result.r1 = visitedBitmap.getBucket(0);
        result.r2 = 0L;
        result.r3 = 0L;
        result.r4 = 0L;
        result.r5 = 0L;
        result.r6 = 0L;
        result.r7 = 0L;
        result.r8 = 0L;
        if (vertexCount > 64) result.r2 = visitedBitmap.getBucket(1);
        if (vertexCount > 128) result.r3 = visitedBitmap.getBucket(2);
        if (vertexCount > 192) result.r4 = visitedBitmap.getBucket(3);
        if (vertexCount > 256) result.r5 = visitedBitmap.getBucket(4);
        if (vertexCount > 320) result.r6 = visitedBitmap.getBucket(5);
        if (vertexCount > 384) result.r7 = visitedBitmap.getBucket(6);
        if (vertexCount > 448) result.r8 = visitedBitmap.getBucket(7);
    }

    static class BFSTask implements Runnable {
        @Override
        public void run() {
            Integer vertex = queue.poll();
            while (vertex != null) {
                visitedBitmap.setBit(vertex);
                for (int next : edges[vertex]) {
                    visitedBitmap.setBit(next);
                }

                vertex = queue.poll();
            }
        }
    }
}
