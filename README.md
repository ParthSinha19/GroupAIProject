# 🍣 Sushi Go! MCTS Agent

## Overview

This project implements a **Monte Carlo Tree Search (MCTS)** agent for the Tabletop Games (TAG) framework version of **Sushi Go!**.

The agent utilizes **UCT (Upper Confidence Bound 1 applied to Trees)** with several optimizations for fast and reliable performance under a fixed time budget:
* **RAVE Blending:** Combines the standard MCTS value estimate with the Rapid Action Value Estimation (RAVE) for faster convergence in the early stages of the search.
* **Heuristic-Seeded Priors:** Uses domain knowledge to bias initial action selection.
* **ε-Greedy Rollouts:** Employs an epsilon-greedy policy during playouts for effective exploration.

A small harness is included to run reproducible **3-player tournaments** against baseline players.

---

## 💻 Requirements

* **Java 17+** (OpenJDK recommended)
* **IntelliJ IDEA** or **VS Code** with Java support
* **Gradle** or **Maven** build tool
* **TAG framework** checked out with the **Sushi Go!** game module enabled.

---

## 📁 Repo Layout

| Path | Description |
| :--- | :--- |
| `src/main/java/GroupAI_parth/...` | Your group package for agent code and helpers. |
| `src/main/java/GroupAI_parth/tournamentPlayers` | Directory scanned by the runner to load all tournament participants. |
| `src/main/java/GroupAI_parth/TestSushiGoAgent.java` | Main entry point to run matches/tournaments (adjust if your runner is different). |
| `configs/` | JSON configuration files for reproducible runs (create this folder if it doesn’t exist). |
| `README.md` | This file. |

---

## 🚀 Quick Start

1.  **Clone and Open:** Clone the project and open it in your chosen IDE.
2.  **Verify Package:** Ensure your base package name is `GroupAI_parth` and the player scan directory is set to `src/main/java/GroupAI_parth/tournamentPlayers`.
3.  **Build:** Build the project (e.g., in Gradle, select "Reload Gradle Project" and then Build).
4.  **Create Config:** Create the `configs/` folder and save the following content as **`configs/tournament.json`**:

    ```json
    {
      "game": "SushiGo",
      "nPlayers": 3,
      "playerDirectory": "src/main/java/GroupAI_parth/tournamentPlayers",
      "matchups": 100,
      "mode": "RANDOM",
      "verbose": true,
      "seed": 740234
    }
    ```

5.  **Run:** Execute the tournament runner (e.g., run the `main` method in `TestSushiGoAgent.java`). Check the console output for results and statistics.

---

## ➕ Adding Players

To add a player to the tournament:

1.  Create a class that **extends `AbstractPlayer`**.
2.  Ensure the class has a **no-argument constructor** or that your runner correctly constructs it.
3.  Place the compiled class file into the following directory:
    `src/main/java/GroupAI_parth/tournamentPlayers`

**Examples:**
* `MySushiGoAgent.java` (Your primary agent)
* `MCTSPlayer.java` (A baseline MCTS implementation)
* `RandomPlayer.java` (A simple random baseline)

---

## ⚙️ Agent Parameters

If your runner uses a parameterized constructor for the agent, the typical parameters are:

| Parameter | Type | Example Value | Description |
| :--- | :--- | :--- | :--- |
| **`timeBudgetMs`** | `int` | `300` | Max milliseconds per move for the MCTS search. |
| **`cParam`** | `double` | `1.5` | UCT exploration constant ($c$). |
| **`kRave`** | `int` | `600` | RAVE weight constant ($K$ in $\beta^*$). |
| **`rolloutEps`** | `double` | `0.5` | $\epsilon$ for $\epsilon$-greedy rollout policy. |
| **`seed`** | `long` | `7L` | Random seed for the agent's internal components. |

**Example Constructor:**
```java
new MySushiGoAgent(300, 1.5, 600, 0.5, 7L);
