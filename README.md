# SortingVisualiser2 — Research Branch

> ⚠️ **This is the `Research` branch.** It contains an experimental, benchmark-focused fork of the Sorting Visualizer with embedded JMH-style benchmarking, raw algorithm implementations, and automated research tooling. For the full application (including the Data Structure Visualizer and Hub), see the [`master`](https://github.com/REGEN-HALOGEN/SortingVisualiser2/tree/master) branch.

A **single-file Java Swing** sorting visualizer with embedded **JMH-quality benchmarking** for research-grade performance analysis of sorting algorithms. This branch strips the project down to the sorting visualizer core and augments it with zero-overhead raw algorithm implementations, warmup iterations, DCE fences, and automated statistical research tooling.

![Java](https://img.shields.io/badge/Java-8%2B-orange?logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Swing-blue)
![Branch](https://img.shields.io/badge/Branch-Research-purple)
![License](https://img.shields.io/badge/License-MIT-green)

---

## 🔬 What Makes This Branch Different

| Feature | Master Branch | Research Branch |
|---------|--------------|-----------------|
| **Scope** | Full app (Sorting + Data Structures + Hub) | Sorting Visualizer only |
| **Benchmarking** | Basic `System.nanoTime()` timing | JMH-style warmup + measurement iterations |
| **Algorithm Implementations** | Tracked only (`TrackedArray`) | Dual: Tracked (visualization) + Raw (benchmarking) |
| **DCE Protection** | None | Dead Code Elimination fences on all timed runs |
| **Thread Model** | Multi-threaded analysis | Single-threaded executor (eliminates CPU contention noise) |
| **Auto Research** | Sizes 100–1,000 | Sizes 100–10,000 (100 size points × 30 trials each) |
| **JMH Module** | Not included | Standalone Maven JMH benchmark module included |

---

## 🔄 Sorting Algorithms

Visualize and benchmark 8 popular sorting algorithms:

| Algorithm | Time Complexity (Avg) | Space Complexity |
|-----------|----------------------|------------------|
| **Bubble Sort** | O(N²) | O(1) |
| **Selection Sort** | O(N²) | O(1) |
| **Insertion Sort** | O(N²) | O(1) |
| **Merge Sort** | O(N log N) | O(N) |
| **Quick Sort** | O(N log N) | O(log N) |
| **Heap Sort** | O(N log N) | O(1) |
| **Shell Sort** | O(N log² N) | O(1) |
| **Radix Sort** | O(NK) | O(N) |

---

## 🧪 Research & Benchmarking Features

### Embedded JMH-Style Benchmarking
- **Warmup Iterations**: 3 warmup runs per algorithm before measurement to trigger JIT compilation
- **Raw Algorithm Implementations**: Zero-overhead sorting functions (`RawSortingAlgorithms`) with no tracking, no operation recording — capturing only the actual sorting work
- **DCE Fences**: Dead Code Elimination barriers on all timed sorting runs to prevent the JVM from optimizing away benchmark code
- **Single-Threaded Execution**: All benchmark runs use a single-threaded executor to eliminate CPU contention noise between concurrent benchmarks

### Auto Research Mode
- Automated batch benchmarking across **100 array sizes** (100 to 10,000 in steps of 100)
- **30 trials per size** per algorithm for statistical significance
- Computes **Mean** and **Standard Deviation** for:
  - Execution time (nanosecond precision)
  - Memory usage
  - Comparisons
  - Swaps
- Results exportable to **CSV** for analysis in Excel, Google Sheets, R, Python, etc.
- Live progress bar with elapsed time and ETA

### Auto Analysis Mode
- Single-run analysis on a custom array size (up to 1,000,000 elements)
- Supports 5 data distributions: **Random**, **Nearly Sorted**, **Reversed**, **Few Unique**, **Gaussian**
- Records: Time, Memory Diff, Peak Aux Elements, Time/Space Complexity, Swaps, Array Writes, Array Reads, Comparisons

### Standalone JMH Benchmark Module
The `jmh-benchmark/` directory contains a Maven-based JMH benchmark project with the same raw algorithm implementations:

```
jmh-benchmark/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/main/java/sorting/benchmark/
    ├── RawSortingAlgorithms.java
    └── SortingBenchmark.java
```

Run standalone JMH benchmarks:
```bash
cd jmh-benchmark
./mvnw clean package
java -jar target/benchmarks.jar
```

---

## ✨ Visualizer Features

- Adjustable array size (1–300 elements) with live size counter display
- Adjustable animation speed
- Random array generation and custom array input
- Toggle number display and auxiliary space visibility
- Pause / Resume / Reset / Clear functionality
- **View Code** panel showing pseudo-code for each algorithm with live line highlighting during playback
- **Sort Analysis** window with full metrics history table
- **Algorithm Comparison** race mode — side-by-side visualization with individual metrics
- **Live Auxiliary Space** visualization with split-screen display and dynamic element counters (Merge Sort, Radix Sort)
- **Tooltips** across all UI elements
- Step playback architecture with batched frame rendering

---

## 📁 Project Structure

```
SortingVisualizerRetcon/
├── SortingVisualiser.java         # Single-file sorting visualizer with embedded benchmarking
├── jmh-benchmark/                 # Standalone JMH benchmark module (Maven)
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/main/java/sorting/benchmark/
│       ├── RawSortingAlgorithms.java
│       └── SortingBenchmark.java
├── .gitignore
└── README.md                      # This file
```

### Key Classes (in SortingVisualiser.java)

| Class | Description |
|-------|-------------|
| `SortingVisualiser` | Entry point, FlatLaf theme setup |
| `VisualFrame` | Main window with all controls and algorithm definitions |
| `VisualPanel` | Custom `paintComponent` rendering of array bars and aux space |
| `SortingAlgorithms` | Tracked sorting algorithms (record operations for visualization) |
| `RawSortingAlgorithms` | Zero-overhead sorting algorithms (for accurate benchmarking) |
| `RawBenchmarkRunner` | Routes benchmark calls to raw implementations by name |
| `TrackedArray` | Proxy wrapper capturing reads, writes, swaps, comparisons |
| `OperationPlayer` | Timer-based operation replayer with batched frame advancement |
| `SortAnalysisDialog` | Analysis UI with Auto Run, Auto Research, CSV Export |
| `CompareFrame` | Side-by-side algorithm race comparison |
| `CodeViewerDialog` | Live code viewer with line-level highlighting |

---

## 🚀 Getting Started

### Prerequisites
- **Java JDK 8** or higher
- (Optional) **FlatLaf** JAR for modern Look & Feel — falls back to system L&F if unavailable

### Compile & Run
```bash
# With FlatLaf (recommended)
javac -cp ".;flatlaf.jar" SortingVisualiser.java
java -cp ".;flatlaf.jar" SortingVisualiser

# Without FlatLaf (uses system Look & Feel)
javac SortingVisualiser.java
java SortingVisualiser
```

### Running a Research Session
1. Launch the application
2. Click **📊 Sort Analysis**
3. Select a data distribution (Random, Nearly Sorted, Reversed, Few Unique, Gaussian)
4. Click **📈 Auto Research** to run the full benchmark suite
5. Wait for completion (progress bar shows ETA)
6. Click **💾 Export to CSV** to save results

---

## 🛠️ Technical Details

- **Language:** Java 8+
- **GUI Framework:** Java Swing with FlatLaf theming
- **Architecture:** Single-file design — all classes in `SortingVisualiser.java`
- **Benchmarking:** JMH-style methodology with warmup, DCE fences, and single-threaded execution
- **Animation:** Timer-based operation playback with batch frame rendering (16ms frame target, up to 32 ops/frame)
- **Metrics:** TrackedArray proxy captures comparisons, swaps, reads, writes, and auxiliary space allocations
- **Rendering:** Custom `paintComponent` with `Graphics2D` for HSB-colored bars and real-time aux space display

---

## 👥 Authors

This project was developed as part of an Algorithm and Data Structures (ADS) course project by:

- **Ashwin**
- **Anirudh**
- **Pravesh**
- **Shivam**

**SIES College of Management Studies**
MCA Department | Batch 2025-27

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 🤝 Contributing

Contributions are welcome! Feel free to:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/NewFeature`)
3. Commit your changes (`git commit -m 'Add NewFeature'`)
4. Push to the branch (`git push origin feature/NewFeature`)
5. Open a Pull Request

---

## 📋 Changelog (Research Branch)

### Research v1.0.0 (April 2026)
**Forked from master v1.4.1 — Research-focused refactoring:**
- 🔬 **JMH-Style Benchmarking**: Embedded warmup iterations (3 rounds) before all timed measurements in Auto Analysis and Auto Research modes
- ⚡ **Raw Algorithm Implementations**: Added `RawSortingAlgorithms` class with zero-overhead pure sorting implementations for accurate timing (no TrackedArray, no Operation recording, no SortMetrics)
- 🛡️ **DCE Fences**: Added Dead Code Elimination barriers (`if (timedCopy[0] == Integer.MIN_VALUE) throw new AssertionError("DCE")`) to prevent JVM from optimizing away benchmark code
- 🧵 **Single-Threaded Benchmarking**: Switched from multi-threaded to single-threaded executor for both Auto Analysis and Auto Research to eliminate CPU contention noise
- 📈 **Extended Research Range**: Auto Research now covers array sizes 100 to 10,000 (100 data points × 30 trials = 3,000 trials per algorithm per distribution)
- 🔁 **Dual Algorithm Architecture**: Tracked algorithms used for visualization and metrics collection; raw algorithms used for timing — ensuring measurement accuracy without sacrificing visual fidelity
- 📦 **Standalone JMH Module**: Included Maven-based JMH benchmark project in `jmh-benchmark/` for external benchmarking validation
- 🎯 **Scope Reduction**: This branch contains only the Sorting Visualizer — no Data Structure Visualizer, no Hub launcher
