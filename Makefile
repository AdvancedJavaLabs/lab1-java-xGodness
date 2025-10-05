jcstress:
	javac -cp jcstress-latest.jar -d out \
	src/test/java/org/itmo/JCStressBFSIterationTest.java \
	src/test/java/org/itmo/RandomGraphGenerator.java \
	src/main/java/org/itmo/AtomicBitmap.java \
	src/main/java/org/itmo/Graph.java
	java -cp out:jcstress-latest.jar org.openjdk.jcstress.Main
	rm jcstress-results-*.bin.gz