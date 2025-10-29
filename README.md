# 🍣 ECS7032P: Sushi Go! AI Agent (GroupAI)

## 🎯 Project Overview

This project implements an enhanced **Monte Carlo Tree Search (MCTS)** agent, named `MySushiGoAgent`, designed to play the card-drafting game **Sushi Go!** within the Java TAG framework.

[cite_start]The agent aims to achieve robust performance in this **Imperfect Information (II)** and **Multi-Player** environment by integrating advanced search heuristics and statistical techniques[cite: 2128].

---

## 💡 Agent Design Highlights (Complexity Level 4)

The core agent architecture is a highly modified form of `BasicMCTS`. It integrates three major enhancements to overcome the challenges of hidden information and a strict 1-second decision limit.

### 1. Imperfect Information Handling: Root Determinization

[cite_start]To counter the problem of **Partial Observability** (hidden hands and unknown deck order), the agent uses the **Root Determinization** strategy[cite: 2128]:
* [cite_start]In every MCTS iteration, the agent creates a **single, fully specified world state** (a *determinization*) that is consistent with the player's observable information (their hand, played cards, scores)[cite: 1711].
* The search then runs solely on this hypothetical state. [cite_start]This method ensures that the accumulated statistics are robust, as they are averaged across thousands of possible hidden scenarios, implicitly handling the core issue of **Strategy Fusion**[cite: 1713].

### 2. Fast Learning: RAVE/AMAF Integration

The agent integrates the **Rapid Action Value Estimate (RAVE)** heuristic to accelerate learning under the strict time budget.
* **AMAF (All Moves As First):** During the **Backpropagation** phase, rewards from the playout are applied not only to the moves directly in the tree path ($Q(s,a)$) but also to **every occurrence of those actions** seen later in the random rollout ($A_a$).
* **Value Blending:** The **Selection** phase uses a dynamically decaying weight ($\alpha$) to blend the two estimates: the fast-converging AMAF value and the slowly converging MCTS value. This provides high reliability early in the search.

$$Q^{\alpha\text{-AMAF}}(s,a) = \alpha \cdot A_a + (1 - \alpha) \cdot Q(s,a)$$

### 3. Search Guidance: Heuristic-Biased Rollouts

[cite_start]To improve efficiency and quality of the search (a form of **soft pruning** [cite: 1122, 3403][cite_start]), the agent uses a domain-specific strategy during the **Playout (Simulation)** phase[cite: 2121].
* The default uniform random policy is replaced by a policy biased using a sophisticated **Sushi Go! Heuristic** (`SushiGoHeuristic.java`).
* [cite_start]This heuristic prioritizes strategic moves like **set completion pressure** (Sashimi, Tempura) and **non-linear scoring** (Dumplings), ensuring the thousands of simulated games yield realistic, high-quality feedback to the learning engine[cite: 2121].

---

## 🚀 Execution Instructions

This agent requires the Java TAG framework to run and must be executed using the `RunGames` main class.

### 1. File Structure

All custom files must be placed within a specific package:

| File | Location |
| :--- | :--- |
| `MySushiGoAgent.java` | `src/main/java/GroupAI/` |
| `RAVENode.java` | `src/main/java/GroupAI/` |
| `SushiGoHeuristic.java` | `src/main/java/GroupAI/` |
| `PlayerParameters.java` | `src/main/java/GroupAI/` |

### 2. How to Run the Agent

Use IntelliJ IDEA's Run Configuration with the following settings.

| Configuration Field | Value | Notes |
| :--- | :--- | :--- |
| **Main class** | `evaluation.RunGames` | The controller for batch testing/competitions. |
| **Program arguments** | `-g SushiGo -p GroupAI.MySushiGoAgent -p players.mcts.BasicMCTS -p players.rhea.RHEAPlayer -i 500 -t 1000` | [cite_start]This command runs **500 games** of **3-player Sushi Go!** with a **1-second** time limit per decision (required for competition testing)[cite: 2062, 2151, 2153]. |

---

## ⚙️ Dependencies

This project relies on the core classes found in the TAG framework, primarily within the `core.*`, `games.sushigo.*`, and `utilities.*` packages.

*(Insert JSON Configuration File for Competition here if required by submission rules)*# GroupAIProject
