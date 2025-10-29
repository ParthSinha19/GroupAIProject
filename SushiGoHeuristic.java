package GroupAI;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.AbstractForwardModel;


import java.util.Random;

/**
 * Static scoring utilities for Sushi Go! draft decisions.
 * Encodes set-completion pressure and endgame bonuses based on the official rules.
 */
public final class SushiGoHeuristic {

    private SushiGoHeuristic() {}

    /**
     * Evaluate the desirability of playing 'action' for 'pid' in state 'st'.
     * Implement using the concrete Sushi Go! state model (table counts and open wasabi).
     * Heuristic elements (approximate marginal values):
     *  - Tempura: +5 on pair completion, +2.5 if one away with turns left
     *  - Sashimi: +10 on triple, +3.5 partial if feasible
     *  - Dumpling: totals [0,1,3,6,10,15] => use marginal delta
     *  - Nigiri/Wasabi: 1/2/3, tripled if paired with open wasabi; bonus if wasabi open
     *  - Maki: +0.5 per icon, +1.5 if this picks/keeps majority lead
     *  - Pudding: +0.6 midgame, higher near end
     */
    public static double evaluateAction(AbstractGameState st,
                                        AbstractForwardModel fm,
                                        int pid,
                                        AbstractAction action,
                                        Random rng) {
        // Apply hypothetical move
        AbstractGameState s2 = st.copy();
        fm.next(s2, action);

        // Read sushi-go specific tables; replace with actual getters in your codebase.
        int remainingTurns = s2.getTurnCounterMax() - s2.getTurnCounter();
        int roundsLeft = s2.getGameParameters().getNMaxRounds() - s2.getRoundCounter();

        // Fetch counts (replace with your state API):
        int tempura = getTempura(s2, pid);
        int sashimi = getSashimi(s2, pid);
        int dumplings = getDumplings(s2, pid);
        int openWasabi = getOpenWasabi(s2, pid);
        int makiIcons = getMakiIcons(s2, pid);
        int puddings = getPuddings(s2, pid);

        double val = 0.0;

        // Tempura pair pressure
        val += (tempura / 2) * 5.0;
        if (tempura % 2 == 1 && remainingTurns > 0) val += 1.5;

        // Sashimi triple pressure
        val += (sashimi / 3) * 10.0;
        int need = sashimi % 3;
        if (need != 0 && remainingTurns >= (3 - need)) val += 3.5;

        // Dumpling curve
        val += dumplingTotal(dumplings);

        // Wasabi synergy / Nigiri payoff
        if (openWasabi > 0) val += 2.0;

        // Maki race
        val += 0.5 * makiIcons;
        if (picksOrKeepsMakiLead(s2, pid, makiIcons)) val += 1.5;

        // Pudding
        double puddingScale = 0.6 + (roundsLeft <= 1 ? 0.8 : 0.0);
        val += puddingScale * puddings;

        return val;
    }

    private static int dumplingTotal(int n) {
        int[] curve = {0, 1, 3, 6, 10, 15}; // rules [web:176]
        return curve[Math.min(n, 5)];
    }

    // ---- The getters below must be implemented for your Sushi Go! state type ----
    private static int getTempura(AbstractGameState s, int pid) { return 0; }
    private static int getSashimi(AbstractGameState s, int pid) { return 0; }
    private static int getDumplings(AbstractGameState s, int pid) { return 0; }
    private static int getOpenWasabi(AbstractGameState s, int pid) { return 0; }
    private static int getMakiIcons(AbstractGameState s, int pid) { return 0; }
    private static int getPuddings(AbstractGameState s, int pid) { return 0; }
    private static boolean picksOrKeepsMakiLead(AbstractGameState s, int pid, int iconsAfter) { return false; }
}
