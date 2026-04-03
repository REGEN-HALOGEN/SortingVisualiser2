package sorting.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark harness for sorting algorithms.
 * <p>
 * Measures the raw execution time of each sorting algorithm across
 * multiple array sizes and data distributions. Uses pure implementations
 * from {@link RawSortingAlgorithms} — no visualisation or tracking overhead.
 *
 * <h3>Build &amp; Run</h3>
 * <pre>
 *   cd jmh-benchmark
 *   mvn clean package
 *   java -jar target/benchmarks.jar                       # full suite
 *   java -jar target/benchmarks.jar ".*bubbleSort"        # single algo
 *   java -jar target/benchmarks.jar -p arraySize=1000     # single size
 *   java -jar target/benchmarks.jar -rf csv               # CSV output
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Thread)
public class SortingBenchmark {

    // ─────────────────────────── Parameters ───────────────────────────

    @Param({"100", "1000", "10000", "100000"})
    private int arraySize;

    @Param({"random", "nearly_sorted", "reversed", "few_unique", "gaussian"})
    private String distribution;

    // ─────────────────────────── State ───────────────────────────

    /**
     * The canonical source array, generated once per (size, distribution)
     * combination at the start of each trial. Never mutated by benchmarks.
     */
    private int[] sourceArray;

    /**
     * A fresh clone of {@code sourceArray} prepared before every single
     * benchmark invocation so that each sort operates on unsorted data.
     */
    private int[] workingCopy;

    // ─────────────────────────── Setup ───────────────────────────

    /**
     * Generate the source array once per trial (per parameter combination).
     * This avoids regeneration overhead inside the measured region.
     */
    @Setup(Level.Trial)
    public void setupTrial() {
        Random rnd = new Random(42); // fixed seed for reproducibility
        sourceArray = new int[arraySize];

        switch (distribution) {
            case "nearly_sorted":
                for (int i = 0; i < arraySize; i++) sourceArray[i] = i + 1;
                // 5% random swaps to introduce mild disorder
                int swaps = (int) (arraySize * 0.05);
                for (int s = 0; s < swaps; s++) {
                    int i1 = rnd.nextInt(arraySize);
                    int i2 = rnd.nextInt(arraySize);
                    int tmp = sourceArray[i1];
                    sourceArray[i1] = sourceArray[i2];
                    sourceArray[i2] = tmp;
                }
                break;

            case "reversed":
                for (int i = 0; i < arraySize; i++) sourceArray[i] = arraySize - i;
                break;

            case "few_unique":
                for (int i = 0; i < arraySize; i++) {
                    sourceArray[i] = (rnd.nextInt(5) + 1) * (arraySize / 5);
                }
                break;

            case "gaussian":
                for (int i = 0; i < arraySize; i++) {
                    int val = (int) (rnd.nextGaussian() * (arraySize / 4.0) + (arraySize / 2.0));
                    sourceArray[i] = Math.max(1, Math.min(arraySize, val));
                }
                break;

            case "random":
            default:
                for (int i = 0; i < arraySize; i++) {
                    sourceArray[i] = rnd.nextInt(arraySize) + 1;
                }
                break;
        }
    }

    /**
     * Clone the source array before every invocation so each sort
     * starts from the same unsorted input.
     */
    @Setup(Level.Invocation)
    public void setupInvocation() {
        workingCopy = sourceArray.clone();
    }

    // ─────────────────────────── Benchmarks ───────────────────────────

    @Benchmark
    public void bubbleSort(Blackhole bh) {
        RawSortingAlgorithms.bubbleSort(workingCopy);
        bh.consume(workingCopy);
    }

    @Benchmark
    public void selectionSort(Blackhole bh) {
        RawSortingAlgorithms.selectionSort(workingCopy);
        bh.consume(workingCopy);
    }

    @Benchmark
    public void insertionSort(Blackhole bh) {
        RawSortingAlgorithms.insertionSort(workingCopy);
        bh.consume(workingCopy);
    }

    @Benchmark
    public void mergeSort(Blackhole bh) {
        RawSortingAlgorithms.mergeSort(workingCopy);
        bh.consume(workingCopy);
    }

    @Benchmark
    public void quickSort(Blackhole bh) {
        RawSortingAlgorithms.quickSort(workingCopy);
        bh.consume(workingCopy);
    }

    @Benchmark
    public void heapSort(Blackhole bh) {
        RawSortingAlgorithms.heapSort(workingCopy);
        bh.consume(workingCopy);
    }

    @Benchmark
    public void shellSort(Blackhole bh) {
        RawSortingAlgorithms.shellSort(workingCopy);
        bh.consume(workingCopy);
    }

    @Benchmark
    public void radixSort(Blackhole bh) {
        RawSortingAlgorithms.radixSort(workingCopy);
        bh.consume(workingCopy);
    }

    // ─────────────────────────── Main ───────────────────────────

    /**
     * Entry point for running benchmarks from the command line.
     * <p>
     * Runs ALL benchmarks by default. Override with JMH CLI flags:
     * <pre>
     *   java -jar target/benchmarks.jar ".*quickSort" -p arraySize=10000
     * </pre>
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SortingBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
