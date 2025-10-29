package GroupAI;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import core.rules.AbstractForwardModel;
import utilities.ElapsedCpuTimer;

import java.util.*;

/**
 * Enhanced BasicMCTS for Sushi Go!:
 *  - Root determinization per iteration
 *  - UCT + RAVE selection
 *  - Heuristic-biased rollouts
 */
public class MySushiGoAgent extends AbstractPlayer {

    private final int timeBudgetMs;
    private final double cParam;
    private final double kRave;
    private final Random rng;

    private final Map<Long, RAVENode> nodeTable = new HashMap<>();

    public MySushiGoAgent(int timeBudgetMs, double cParam, double kRave, long seed) {
        this.timeBudgetMs = timeBudgetMs;
        this.cParam = cParam;
        this.kRave = kRave;
        this.rng = new Random(seed);
    }
    public MySushiGoAgent() { this(1000, 1.41, 1500.0, 7L); }

    @Override
    public AbstractAction getAction(AbstractGameState rootObs, List<AbstractAction> actions) {
        long key = infoStateKey(rootObs, getPlayerID());
        RAVENode root = nodeTable.computeIfAbsent(key, k -> RAVENode.fromLegal(actions, key));

        ElapsedCpuTimer timer = new ElapsedCpuTimer();
        timer.setMaxTimeMillis(timeBudgetMs);

        while (!timer.exceededMaxTime()) {
            // Root determinization: replace this stub with your SushiGo state logic
            AbstractGameState det = generateDeterminization(rootObs.copy(), getPlayerID(), rng);
            runIteration(det, root);
        }

        // choose best child
        AbstractAction best = null;
        int bestVisits = -1; double bestQ = -1e18;
        for (AbstractAction a : actions) {
            int nsa = root.getNsa(a);
            double q = root.getQ(a);
            if (nsa > bestVisits || (nsa == bestVisits && q > bestQ)) {
                best = a; bestVisits = nsa; bestQ = q;
            }
        }
        return best != null ? best : actions.get(rng.nextInt(actions.size()));
    }

    private void runIteration(AbstractGameState st, RAVENode root) {
        AbstractForwardModel fm = st.getForwardModel().copy();

        Deque<RAVENode> nodePath = new ArrayDeque<>();
        Deque<AbstractAction> actPath = new ArrayDeque<>();
        nodePath.add(root);

        RAVENode node = root;
        while (!st.isTerminal()) {
            List<AbstractAction> legal = fm.computeAvailableActions(st);
            node.ensureActions(legal);

            if (node.hasUntried()) {
                AbstractAction a = node.popUntried(rng);
                fm.next(st, a);

                long childKey = infoStateKey(st, getPlayerID());
                RAVENode child = nodeTable.computeIfAbsent(
                        childKey, k -> RAVENode.fromLegal(fm.computeAvailableActions(st), childKey));
                node.linkChild(a, child);
                actPath.add(a);
                node = child;
                nodePath.add(node);
                break;
            } else {
                AbstractAction a = node.selectByUcbRave(cParam, kRave, rng);
                fm.next(st, a);
                actPath.add(a);
                node = node.childOf(a);
                if (node == null) break;
                nodePath.add(node);
            }
        }

        double R = rollout(st, fm);

        Set<AbstractAction> seenAfter = new HashSet<>();
        Iterator<RAVENode> ni = nodePath.descendingIterator();
        Iterator<AbstractAction> ai = actPath.descendingIterator();
        while (ni.hasNext()) {
            RAVENode n = ni.next();
            if (!ai.hasNext()) break;
            AbstractAction a = ai.next();
            n.backpropEdge(a, R);
            for (AbstractAction aa : seenAfter) n.backpropAMAF(aa, R);
            seenAfter.add(a);
        }
    }

    // Stub: implement using SushiGo state API
    private AbstractGameState generateDeterminization(AbstractGameState st, int myId, Random rng) {
        return st;
    }

    private long infoStateKey(AbstractGameState st, int pid) {
        return Objects.hash(st.getRoundCounter(), st.getTurnCounter(), pid);
    }

    private double rollout(AbstractGameState st, AbstractForwardModel fm) {
        while (!st.isTerminal()) {
            List<AbstractAction> legal = fm.computeAvailableActions(st);
            if (legal.isEmpty()) break;
            AbstractAction a = pickEpsGreedy(st, fm, legal, 0.15);
            fm.next(st, a);
        }
        double raw = st.getGameScore(getPlayerID());
        return Math.max(0.0, Math.min(1.0, raw / 30.0));
    }

    private AbstractAction pickEpsGreedy(AbstractGameState st, AbstractForwardModel fm,
                                         List<AbstractAction> legal, double eps) {
        if (rng.nextDouble() < eps) return legal.get(rng.nextInt(legal.size()));
        double best = -1e18; AbstractAction bestA = legal.get(0);
        for (AbstractAction a : legal) {
            double h = SushiGoHeuristic.evaluateAction(st, getPlayerID(), a, rng);
            if (h > best) { best = h; bestA = a; }
        }
        return bestA;
    }

    @Override
    public AbstractPlayer copy() {
        return new MySushiGoAgent(timeBudgetMs, cParam, kRave, rng.nextLong());
    }
}
