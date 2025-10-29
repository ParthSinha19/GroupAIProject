package GroupAI;

import core.actions.AbstractAction;

import java.util.*;

/**
 * Node storing per-action MCTS and AMAF statistics and providing UCT-RAVE selection.
 * Statistics kept per (s,a):
 *  - Nsa: visit count
 *  - QSum: cumulative reward (Q = QSum / Nsa)
 *  - NA: AMAF count
 *  - QASum: cumulative AMAF reward (A = QASum / NA)
 */
public class RAVENode {

    private int N; // node visits
    private final long infoKey;

    private final Map<AbstractAction, RAVENode> children = new HashMap<>();
    private final List<AbstractAction> untried = new ArrayList<>();

    private final Map<AbstractAction, Integer> Nsa = new HashMap<>();
    private final Map<AbstractAction, Double> QSum = new HashMap<>();
    private final Map<AbstractAction, Integer> NA = new HashMap<>();
    private final Map<AbstractAction, Double> QASum = new HashMap<>();

    private RAVENode(long infoKey) {
        this.infoKey = infoKey;
        this.N = 0;
    }

    public static RAVENode fromLegal(List<AbstractAction> legal, long key) {
        RAVENode n = new RAVENode(key);
        n.ensureActions(legal);
        return n;
    }

    public void ensureActions(List<AbstractAction> legal) {
        for (AbstractAction a : legal) {
            if (!Nsa.containsKey(a)) {
                Nsa.put(a, 0);
                QSum.put(a, 0.0);
                NA.put(a, 0);
                QASum.put(a, 0.0);
                untried.add(a);
            }
        }
    }

    public boolean hasUntried() { return !untried.isEmpty(); }
    public AbstractAction popUntried(Random rng) { return untried.remove(rng.nextInt(untried.size())); }

    public void linkChild(AbstractAction a, RAVENode child) { children.put(a, child); }
    public RAVENode childOf(AbstractAction a) { return children.get(a); }

    public int getNsa(AbstractAction a) { return Nsa.getOrDefault(a, 0); }
    public double getQ(AbstractAction a) {
        int n = Nsa.getOrDefault(a, 0);
        return n == 0 ? 0.0 : QSum.get(a) / n;
    }

    // Selection with UCB1 using hybrid Qmix = (1 - α) Q + α A, α = k / (k + N)
    public AbstractAction selectByUcbRave(double cParam, double kRave, Random rng) {
        double lnN = Math.log(Math.max(1, N));
        double bestScore = -1e18;
        AbstractAction bestA = null;

        for (AbstractAction a : Nsa.keySet()) {
            int nsa = Nsa.getOrDefault(a, 0);
            int na = NA.getOrDefault(a, 0);
            double q = nsa == 0 ? 0.0 : QSum.get(a) / nsa;
            double amaf = na == 0 ? 0.0 : QASum.get(a) / na;

            double alpha = kRave / (kRave + Math.max(1, N)); // decays with node visits [web:190][web:182]
            double qmix = (1.0 - alpha) * q + alpha * amaf;

            double ucb = (nsa == 0) ? Double.POSITIVE_INFINITY
                    : cParam * Math.sqrt(lnN / nsa);

            double score = qmix + ucb;
            if (score > bestScore) {
                bestScore = score;
                bestA = a;
            }
        }
        // Random tie-breaker if needed
        if (bestA == null) {
            List<AbstractAction> keys = new ArrayList<>(Nsa.keySet());
            bestA = keys.get(rng.nextInt(keys.size()));
        }
        return bestA;
    }

    // Backprop for edge (s,a)
    public void backpropEdge(AbstractAction a, double R) {
        N += 1;
        Nsa.put(a, Nsa.getOrDefault(a, 0) + 1);
        QSum.put(a, QSum.getOrDefault(a, 0.0) + R);
    }

    // AMAF backprop for action a seen later in rollout
    public void backpropAMAF(AbstractAction a, double R) {
        NA.put(a, NA.getOrDefault(a, 0) + 1);
        QASum.put(a, QASum.getOrDefault(a, 0.0) + R);
    }
}
