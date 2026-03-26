 = Get-Content README.md -Raw
 =  -replace ""(?s)(?=### 🔗 Data Structure Visualizer)"", ""- **Sort Analysis**: Dedicated statistics window tracking scientific metrics including Time, Memory Usage, Space Complexity, Swaps, Comparisons, Array Reads, and Array Writes.
- **Algorithm Comparison**: Side-by-side race mode comparing two algorithms visually with individual metrics, adjustable array sizes, and toggleable number overlays.
- **Research-grade Accuracy**: Employs a proxy wrapper pattern to capture true mathematical array operations outperforming pure visual updates.

""
 =  -replace ""(?s)(?=### v1.2.1 \(March 2026\))"", ""### v1.3.0 (March 2026) - Shastra
**Sorting Visualizer Enhancements:**
- ✨ **Sort Analysis**: New metrics window detailing exact algorithm performance (Time, Memory Diff, Space Complexity, Array Reads, Array Writes, Swaps, Comparisons).
- 🏁 **Algorithm Comparison Race**: Added a side-by-side window to race two algorithms simultaneously with toggleable numbers and adjustable array bounds.
- 🔬 **Research-Grade Metric Proxy**: Reworked core sorting structures to use TrackedArray bindings ensuring 100% accurate mathematical recording suitable for scientific papers.

""
 | Set-Content README.md -Encoding utf8
