import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private ArrayList<Boolean> followees;
    private Set<Transaction> toSend;
    private Set<Transaction> agreed;
    private int numRounds;
    private int currentRound;
    private Set<Integer> blacklisted;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.followees = new ArrayList<>();
        this.toSend = new HashSet<>();
        this.agreed = new HashSet<>();
        this.numRounds = numRounds;
        this.currentRound = 0;
        this.blacklisted = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        for (int i = 0; i < followees.length; i++) {
            this.followees.add(followees[i]);
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.toSend.addAll(pendingTransactions);
        this.agreed.addAll(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        if (currentRound >= numRounds) {
            return new HashSet<>(agreed);
        }

        currentRound++;
        Set<Transaction> txns = new HashSet<>(toSend);
        toSend.clear();
        return txns;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        Set<Integer> senders = new HashSet<>();
        for (Candidate candidate : candidates) {
            senders.add(candidate.sender);
        }

        for (int i = 0; i < followees.size(); i++) {
            if (followees.get(i) && !senders.contains(i)) {
                blacklisted.add(i);
            }
        }

        for (Candidate c : candidates) {
            if (!followees.get(c.sender) || blacklisted.contains(c.sender)) {
                continue;
            }
            if (!agreed.contains(c.tx)) {
                agreed.add(c.tx);
                toSend.add(c.tx);
            }
        }
    }
}
