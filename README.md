# 📊 Visualizer Hub - Sorting & Data Structure Visualizer

A comprehensive **Java Swing** application that provides interactive visualizations of popular sorting algorithms and fundamental data structures. Built for educational purposes to help understand algorithm behavior and data structure operations through real-time visual animations.

![Java](https://img.shields.io/badge/Java-8%2B-orange?logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Swing-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## ✨ Features

### 🔄 Sorting Visualizer
Visualize 8 popular sorting algorithms with step-by-step animations:

| Algorithm | Time Complexity (Avg) | Space Complexity |
|-----------|----------------------|------------------|
| **Bubble Sort** | O(n²) | O(1) |
| **Selection Sort** | O(n²) | O(1) |
| **Insertion Sort** | O(n²) | O(1) |
| **Merge Sort** | O(n log n) | O(n) |
| **Quick Sort** | O(n log n) | O(log n) |
| **Heap Sort** | O(n log n) | O(1) |
| **Shell Sort** | O(n log n) | O(1) |
| **Radix Sort** | O(nk) | O(n + k) |

**Features:**
- Adjustable array size (10-300 elements) with **live size counter display**
- Adjustable animation speed
- Random array generation
- Custom array input support (fixed first-load bug)
- Toggle number display on/off
- Pause/Resume/Reset functionality
- **Clear button** to reset to blank state (default on launch)
- **Completion stats** showing operation count and execution time
- **View Code** panel for the selected sorting algorithm
- Step playback architecture with line-aware operation replay

- **Sort Analysis**: Dedicated statistics window tracking scientific metrics including Time, Memory Usage, Space Complexity, Swaps, Comparisons, Array Reads, and Array Writes.
- **Algorithm Comparison**: Side-by-side race mode comparing two algorithms visually with individual metrics, adjustable array sizes, and toggleable number overlays.
- **Research-grade Accuracy**: Employs a proxy wrapper pattern to capture true mathematical array operations outperforming pure visual updates.
- **Tooltips** added across all UI elements to clarify function behavior.
- **Custom Data Distributions** added (Nearly Sorted, Reversed, Few Unique, Gaussian).
- **Live Auxiliary Space Indication** with visual array split screens and dynamic element counters.

### 🔗 Data Structure Visualizer
Interactive visualization of 6 fundamental data structures:

| Data Structure | Type | Description |
|----------------|------|-------------|
| **Stack** | LIFO | Last In, First Out |
| **Queue** | FIFO | First In, First Out |
| **Dequeue** | Double-Ended | Add/Remove from both ends |
| **LinkedList** | Singly Linked | One-way traversal |
| **Circular LinkedList** | Circular | Last node points to first (HEAD & TAIL visible) |
| **Doubly LinkedList** | Two-way | Forward and backward traversal |

**Features:**
- Add/Remove elements interactively
- **Insert at any position** - beginning, specific index, or end (LinkedLists)
- **Delete at any position** - beginning, specific index, or end (LinkedLists)
- **Custom size input** for random fill
- **Strict capacity enforcement** for bounded structures such as Stack, Queue, and Dequeue
- **Clear validation messages** for invalid operations, overflow, underflow, and bad indexes
- **Search functionality** with visual highlight and selection feedback
- **Value preservation** when switching between data structures
- Random fill functionality with unique values
- **Zoom controls** with buttons and `Ctrl + mouse wheel` for focused structure inspection
- Memory address / node ID simulation for linked structures
- Visual pointer arrows between nodes
- **Node inspector panel** showing value, node ID, next reference, previous reference, and node details on click
- **View Code** panel for structure operations such as push, pop, enqueue, insert, delete, and traversal
- Improved animated transitions for insertion, deletion, movement, and traversal emphasis
- Linked-list operations now route through structure-aware index handling so pointer metadata stays correct
- Linked lists allow duplicate values, while bounded linear structures enforce stricter validation

### 🎨 Modern UI
- Minimal hub dashboard with centered responsive layout
- Light and dark mode toggle from the top navigation
- Rounded launcher cards with hover effects
- Clickable GitHub attribution footer in the hub
- Smooth animations and visual feedback

---

## 🚀 Getting Started

### Prerequisites
- **Java JDK 8** or higher
- Terminal/Command Prompt

### Installation & Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/REGEN-HALOGEN/SortingVisualiser2.git
   cd SortingVisualiser2
   ```

2. **Compile the Java files:**
   ```bash
   javac VisualizerHub.java SortingVisualiser.java DataStructureVisualizer.java
   ```

3. **Run the application:**
   ```bash
   java VisualizerHub
   ```

## ⬇️ Download the Windows App

**VisualizerHub** is a Java Swing desktop app for exploring sorting algorithms and data structures through interactive visualizations.

- Releases page: [GitHub Releases](https://github.com/REGEN-HALOGEN/SortingVisualiser2/releases)
- Latest Windows ZIP: [Download the latest VisualizerHub ZIP](https://github.com/REGEN-HALOGEN/SortingVisualiser2/releases/latest/download/VisualizerHub-1.3.2-windows-portable.zip)

### How to run
1. Open the latest release from the GitHub Releases page.
2. Download the Windows ZIP file.
3. Extract the ZIP anywhere on your PC.
4. Open the extracted folder and run `VisualizerHub.exe`.

No separate Java installation is required for the packaged Windows release.

### Running Individual Visualizers

You can also run each visualizer independently:

```bash
# Sorting Visualizer only
java SortingVisualiser

# Data Structure Visualizer only
java DataStructureVisualizer
```

---

## 📁 Project Structure

```
SortingVisualiser2/
├── VisualizerHub.java           # Main hub application (entry point)
├── SortingVisualiser.java       # Sorting algorithms visualizer
├── DataStructureVisualizer.java # Data structures visualizer
├── SortingVisualizerApp/        # Packaged Windows release artifacts
├── ADS_Project_Documentation.pdf # Project documentation
├── .gitignore                    # Ignore generated build artifacts
└── README.md                     # This file
```

> Compiled `.class` files are treated as generated artifacts and are intentionally excluded from version control.

### Key Classes

| File | Main Classes | Description |
|------|-------------|-------------|
| `VisualizerHub.java` | `VisualizerHub`, `HubFrame` | Main hub with navigation to both visualizers |
| `SortingVisualiser.java` | `SortingVisualiser`, `VisualFrame`, `VisualPanel`, `SortingAlgorithms`, `OperationPlayer` | Sorting visualization engine |
| `DataStructureVisualizer.java` | `DataStructureVisualizer`, `DSVisualizerFrame`, `DSVisualizerPanel`, `StackDS`, `QueueDS`, `LinkedListDS`, etc. | Data structure visualization engine |

---

## 🎮 Usage Guide

### Sorting Visualizer
1. Select an algorithm from the dropdown menu
2. Adjust array size using the Size slider
3. Adjust animation speed using the Speed slider
4. Click **Randomize** to generate a new array or **Load Custom Array** to input your own values
5. Click **Start** to begin the visualization
6. Use **Pause** to pause/resume and **Reset** to restore the original array

### Data Structure Visualizer
1. Select a data structure from the dropdown
2. Enter a value in the input field
3. Click **Push/Add** to add elements
4. Click **Pop/Remove** to remove elements
5. For LinkedLists:
   - Use **Insert at Begin/Index/End** for positional insertion
   - Use **Delete Begin/Index/End** for positional deletion
6. Use **Search** to highlight a value and inspect the matching node
7. Click a node/box to open its details in the inspector panel
8. Use **View Code** to open operation-specific reference code
9. Click **Random Fill** to populate with random values
10. Use **Zoom In/Zoom Out** or hold `Ctrl` and scroll the mouse wheel to scale the structure drawing
11. Click **Clear** to empty the data structure

---

## 🛠️ Technical Details

- **Language:** Java 8+
- **GUI Framework:** Java Swing
- **Architecture:** Single-file components with modular class design
- **Animation:** Timer-based operation playback for smooth visualizations
- **Rendering:** Custom `paintComponent` with Graphics2D for anti-aliased graphics

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

## 📚 Documentation

For detailed project documentation, refer to the included `ADS_Project_Documentation.pdf` file.

---

## 📝 Changelog

### v1.4.0 (March 2026)
**UI & Experience Modernization:**
- ✨ **FlatLaf Integration**: Integrated FlatLaf for a seamless, modern, advanced UI Look & Feel that functions entirely without external image assets.
- 🌓 **Global Theme Synchronization**: Introduced a centralized Dark/Light mode toggle in the Hub that automatically cascades and applies to all active visualizer windows on the fly.
- 🔣 **Unicode Iconography**: Replaced complex external images with universally supported Unicode and Emoji symbols directly on buttons (e.g. ✖ for Clear) ensuring reliable cross-system rendering.
- 👁️ **Auxiliary Space Toggle**: Added a "Show Aux" checkbox with tooltips to dynamically toggle the visibility of the auxiliary space display (useful for Merge/Radix sort) which instantly resizes the main array view when hidden.

### v1.3.3 (March 2026)
**Sorting Visualizer Enhancements:**
- ✨ **Custom Datasets**: Built in generators for Gaussian, Reverse, Few Unique, Nearly Sorted.
- 🗄️ **Tracking Auxiliary Space Visuals**: Native tracker for observing temporary `tmp` space operations dynamically during algorithms like Merge Sort and Radix Sort.
- 🖱️ **Tooltips & UI Clear**: Helpful descriptive tooltips describing buttons across tools, plus a new "Clear Data" feature for the analysis windows.

### v1.3.2 (March 2026)
**Sorting Visualizer Enhancements:**
- 🚀 **Parallel Auto Analysis**: Algorithms automatically harness multi-core CPU threading to crunch benchmarks concurrently, functionally eliminating previous single-thread UI lag and dropping latency severely on million-value workloads.
- 🕰️ **Time Complexity Logging**: Introduced structural Time Complexity notation across stats charts alongside runtime bytes/memory outputs.

### v1.3.1 (March 2026)
**Sorting Visualizer Enhancements:**
- ⚙️ **Auto Analysis Module**: Added an automated benchmark runner to the Sort Analysis window. Users can input exceptionally large, custom array setups up to `1,000,000` elements—the runner rapidly benchmarks all algorithms accurately headlessly without stalling the main UI process or wasting graphics resources.
- 💾 **CSV Result Export**: Extracted tracking data (Time, Memory Diff, Space Complexity, Array Reads, Array Writes, Swaps, Comparisons) can be directly exported out of the testing run as a `.csv` file perfectly formated for opening in Excel or Sheets software datasets.

### v1.3.0 (March 2026) - Shastra
**Sorting Visualizer Enhancements:**
- ✨ **Sort Analysis Window**: New metrics window detailing exact algorithm performance (Time, Memory Diff, Space Complexity, Array Reads, Array Writes, Swaps, Comparisons).
- 🏁 **Algorithm Comparison Race**: Added a side-by-side window to race two algorithms simultaneously with toggleable numbers and adjustable array bounds.
- 🔬 **Research-Grade Metric Proxy**: Reworked core sorting structures to use TrackedArray bindings ensuring mathematically rigorous telemetry for scientific datasets.

### v1.2.1 (March 2026)
**Data Structure Visualizer Enhancements:**
- ✅ **Live structure size**: current element count now updates automatically in the status row
- ✅ **Random fill layout polish**: fill count input is placed beside the random fill action for a clearer flow
- ✅ **View zoom controls**: added dedicated zoom in and zoom out buttons for the data-structure canvas
- ✅ **Ctrl + mouse wheel zoom**: hold Ctrl and use the mouse wheel to zoom the structure drawing interactively
- ✅ **Stable header layout while zooming**: the title area stays fixed while only the data-structure graphics scale

### v1.2.0 (March 2026)
**Visualizer Hub Enhancements:**
- ✅ **Minimal landing page refresh**: cleaner hierarchy, more balanced spacing, and simplified top navigation
- ✅ **Theme toggle**: added light/dark mode switching with sun/moon control
- ✅ **Responsive card layout polish**: centered cards and footer alignment now stay visually balanced when resizing
- ✅ **Attribution footer**: added "Built by REGEN-HALOGEN" and clickable GitHub profile link

**Data Structure Visualizer Enhancements:**
- ✅ **Deque validation fixes**: front/rear insertion now respects the configured capacity and rejects invalid operations cleanly
- ✅ **Index operation fixes**: linked-list insertion and deletion now use structure-aware index logic instead of bypassing pointer metadata
- ✅ **Consistent error handling**: clear status messages are shown for overflow, underflow, invalid indexes, and unsupported actions
- ✅ **Improved animations**: insertions, deletions, highlights, and pointer transitions animate more smoothly
- ✅ **Node inspector**: clicking any visible element opens a detail panel with value, node ID, and reference information
- ✅ **Operation code viewer**: added a dedicated code panel for stack, queue, deque, and linked-list operations
- ✅ **Constraint enforcement review**: stack, queue, deque, singly linked list, circular linked list, and doubly linked list now enforce constraints more consistently

**Sorting Visualizer Enhancements:**
- ✅ **Algorithm code viewer**: reusable formatted code panel for the selected sorting algorithm
- ✅ **Operation playback polish**: step-based replay, highlighting, and status updates remain documented in the UI flow

### v1.1.0 (January 2026)
**Sorting Visualizer Enhancements:**
- ✨ **Size Counter Display**: Added a visible box next to the "Size:" label showing the current array size in real-time
- 🐛 **Custom Array Bug Fix**: Fixed issue where custom array values were overwritten by random values on first load
- 🧹 **Clear Button**: Added "Clear" button to reset visualization to blank state; app now starts blank by default
- ⏱️ **Execution Time Tracking**: Status now displays completion message with operation count and elapsed time:
  `Status : Completed Sorting in (X Operations) and (Y Time)`

**Data Structure Visualizer Enhancements:**
- 🆕 **Dequeue Data Structure**: Added double-ended queue with add/remove from both front and rear
- 🎨 **Improved Label Visibility**: Changed TOP/FRONT/REAR labels from black to bright, visible colors
- 🔍 **Search Functionality**: Added search button for Circular LinkedList with index display
- 📍 **Position-Based Insert/Delete**: Middle insert/delete now prompts for specific position index
- 🚫 **Duplicate Prevention**: Linear bounded structures reject duplicate values
- 💾 **Value Preservation**: Values are preserved when switching between data structures
- 📏 **Custom Size Input**: Added size input field for Random Fill (1-50 elements)
- 🏷️ **TAIL Label**: Added visible TAIL label to Circular LinkedList visualization

