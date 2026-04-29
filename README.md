# Phylogenetic Tree Builder

**Phylogenetic Tree Builder**

---

## Overview

PhyloTree is a simple full-stack bioinformatics project that takes DNA or protein sequences as input and generates a **phylogenetic tree** showing how closely related those sequences are.

It is designed for beginners to understand both:

* Basic evolutionary biology concepts
* How a frontend and backend communicate

---

## Tech Stack

* **Backend:** Java 17, Spring Boot 3
* **Frontend:** HTML, CSS, JavaScript
* **Build Tool:** Maven
* **Algorithms:**

  * UPGMA
  * Neighbor-Joining

---

## What It Does

### Input:

* A set of biological sequences (e.g., DNA like `ATCGATCG`)

### Output:

* Phylogenetic tree (3 formats):

  * Dendrogram (rectangular)
  * Radial tree
  * ASCII tree
* Distance matrix (pairwise sequence differences)

---

## Project Structure

```
phylo-tree/
│
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/phylotree/
│       ├── PhyloTreeApplication.java
│       ├── controller/
│       ├── service/
│       └── model/
│
├── frontend/
│   └── index.html
```

---

## How It Works

1. User enters sequences
2. Frontend sends a **POST request (JSON)** to:

   ```
   http://localhost:8080/api/tree
   ```
3. Backend:

   * Computes distances
   * Runs selected algorithm
   * Returns tree as JSON
4. Frontend:

   * Reads JSON
   * Draws the tree visually

---

## Algorithms Used

### 1. UPGMA

* Simple clustering method
* Assumes equal evolution rates (molecular clock)
* Produces ultrametric trees

### 2. Neighbor-Joining

* More advanced and accurate
* Does NOT assume equal rates
* Better for real biological data

---

## Distance Calculation

Uses **p-distance**:

```
p-distance = (# of differing positions) / (total positions)
```

Example:

```
ATCG
ATGG
→ 1 difference out of 4 → distance = 0.25
```

---

## Features

* Multiple tree visualizations
* Distance matrix with highlights

---

## Real-World Applications

* Tracking virus evolution (e.g., COVID variants)
* Cancer evolution studies
* Conservation biology
* Forensic analysis

---

## Key Concepts

* **Phylogenetic Tree:** Evolutionary relationship diagram
* **Node:** Represents a species or ancestor
* **Branch Length:** Evolutionary distance
* **REST API:** Communication between frontend & backend
* **JSON:** Data format for requests/responses

---


---
