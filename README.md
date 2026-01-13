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

### 🔗 Data Structure Visualizer
Interactive visualization of 5 fundamental data structures:

| Data Structure | Type | Description |
|----------------|------|-------------|
| **Stack** | LIFO | Last In, First Out |
| **Queue** | FIFO | First In, First Out |
| **LinkedList** | Singly Linked | One-way traversal |
| **Circular LinkedList** | Circular | Last node points to first |
| **Doubly LinkedList** | Two-way | Forward and backward traversal |

**Features:**
- Add/Remove elements interactively
- **Insert at any position** - beginning, middle, or end (LinkedLists)
- **Delete at any position** - beginning, middle, or end (LinkedLists)
- Random fill functionality
- Memory address simulation for linked structures
- Visual pointer arrows between nodes

### 🎨 Modern UI
- Dark theme with gradient backgrounds
- Rounded cards with hover effects
- Custom styled navigation buttons with icons
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
├── ADS_Project_Documentation.pdf # Project documentation
├── *.class                       # Compiled Java bytecode files
└── README.md                     # This file
```

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
   - Use **Insert at Begin/Middle/End** for positional insertion
   - Use **Delete Begin/Middle/End** for positional deletion
6. Click **Random Fill** to populate with random values
7. Click **Clear** to empty the data structure

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
- **Ajinkya**
- **Prashant**

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

### v1.1.0 (January 2026)
**Sorting Visualizer Enhancements:**
- ✨ **Size Counter Display**: Added a visible box next to the "Size:" label showing the current array size in real-time
- 🐛 **Custom Array Bug Fix**: Fixed issue where custom array values were overwritten by random values on first load
- 🧹 **Clear Button**: Added "Clear" button to reset visualization to blank state; app now starts blank by default
- ⏱️ **Execution Time Tracking**: Status now displays completion message with operation count and elapsed time:
  `Status : Completed Sorting in (X Operations) and (Y Time)`
