# Sorting Visualizer

A Java Swing application that provides an interactive visualization of various sorting algorithms. This tool allows users to see how different sorting algorithms work step-by-step, with customizable array sizes, speeds, and input options.

## Features

- **Supported Algorithms**:
  - Bubble Sort
  - Selection Sort
  - Insertion Sort
  - Merge Sort
  - Quick Sort
  - Heap Sort
  - Shell Sort
  - Radix Sort

- **Interactive Controls**:
  - Select sorting algorithm from a dropdown.
  - Adjust array size (10-300 elements).
  - Control animation speed.
  - Randomize array or load a custom array.
  - Start, pause, and reset the sorting process.
  - Toggle visibility of numbers on bars.

- **Visualization**:
  - Bars represent array elements, with height proportional to value.
  - Red highlights indicate comparisons or swaps.
  - Gradient colors for better visual distinction.

## Requirements

- Java 8 or higher.

## How to Compile and Run

1. Ensure you have Java installed on your system.
2. Navigate to the project directory.
3. Compile the source file:
   ```
   javac SortingVisualiser.java
   ```
4. Run the application:
   ```
   java SortingVisualiser
   ```

## Usage

- Launch the application.
- Choose an algorithm from the dropdown.
- Adjust size and speed sliders as needed.
- Click "Randomize" for a new random array or "Load Custom Array" to input your own values (comma-separated integers).
- Click "Start" to begin the visualization.
- Use "Pause" to halt and resume, or "Reset" to restore the original array.

## Contributing

Feel free to fork the repository and submit pull requests for improvements or additional algorithms.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
