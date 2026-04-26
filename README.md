# COMP20290-Algorithms-25-26-Group12
## Improving Counting Sort Algorithm Via Data Locality

### Overview

This project replicates and extends the paper *"Improving Counting Sort Algorithm via Data Locality"*.  
The goal is to demonstrate how memory locality affects performance and to evaluate a hybrid algorithm that combines QuickSort preprocessing with local Counting Sort.

---
### Project Structure

src/  
├── CountingSort.java  
├── QuickSort.java  
├── QuickSortInsertion.java  
├── QuickWithCountHybridSort.java  
├── BenchmarkAnalysis.java  

---

### File Descriptions

- **CountingSort.java**  
  Implements the classic counting sort algorithm with an additional `sortRange` method for subarray sorting.

- **QuickSort.java**  
  Standard QuickSort using median-of-three pivot selection and Lomuto partitioning.

- **QuickSortInsertion.java**  
  Hybrid QuickSort that switches to insertion sort for small subarrays.

- **QuickWithCountHybridSort.java**  
  Proposed hybrid algorithm combining QuickSort preprocessing with Counting Sort.

- **BenchmarkAnalysis.java**  
  Runs experiments and generates performance results.

---

### Compilation

Navigate to the project root and run:

```bash
javac src/*.java
```

Running the Benchmark Program:
```bash
java -cp src BenchmarkAnalysis
```

### Experiments

#### Table 1 — Counting Sort: Random vs Sorted Input
- Compare counting sort on:
  - Random input
  - Sorted input
- Demonstrates cache locality effects

#### Table 2 — Large Range Scenario (*r* ≫ *n*)
- Case where range *r* ≫ *n*
- Compare:
  - Classic Counting Sort
  - Hybrid algorithm

#### Table 3 Algorithm Comparison
- Compare:
  - QuickSort
  - QuickSort + Insertion
  - Hybrid algorithm