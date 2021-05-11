import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS

        HashSet<UTXO> usedUtxo = new HashSet<>();
        double inputSum = 0;
        double outputSum = 0;

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // (1)
            if (!this.utxoPool.contains(utxo)) {
                return false;
            }

            // (2)
            Transaction.Output output = this.utxoPool.getTxOutput(utxo);
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature)) {
                return false;
            }

            // (3)
            if (usedUtxo.contains(utxo)) {
                return false;
            }
            usedUtxo.add(utxo);

            inputSum += output.value;
        }

        for (int i = 0; i < tx.numOutputs(); i++) {
            // (4)
            Transaction.Output output = tx.getOutput(i);
            if (output.value < 0) {
                return false;
            }

            outputSum += output.value;
        }

        // (5)
        if (inputSum < outputSum) {
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> accepted = new ArrayList<>();
        for (int i = 0; i < possibleTxs.length; i++) {
            Transaction tx = possibleTxs[i];
            if (isValidTx(tx)) {
                ArrayList<Transaction.Input> inputs = tx.getInputs();
                for (int j = 0; j < inputs.size(); j++) {
                    Transaction.Input input = inputs.get(j);
                    utxoPool.removeUTXO(new UTXO(input.prevTxHash, input.outputIndex));
                }
                ArrayList<Transaction.Output> outputs = tx.getOutputs();
                for (int j = 0; j < outputs.size(); j++) {
                    utxoPool.addUTXO(new UTXO(tx.getHash(), j), outputs.get(j));
                }

                accepted.add(tx);
            }
        }

        return accepted.toArray(new Transaction[0]);
    }

    public UTXOPool getUTXOPool() {
        return new UTXOPool(utxoPool);
    }

}
