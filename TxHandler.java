import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxHandler {

	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	private UTXOPool utxoPool;

	public TxHandler(UTXOPool utxoPool) {
		utxoPool = UTXOPool(utxoPool);
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		
		UTXOPool txPool = UTXOPool();
		double sumInput = 0, sumOutput = 0;
		for (int i = 0; i < tx.numInputs(); i++) {
			Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
			Transaction.Output output = utxoPool.getTxOutput(utxo);

			// (1) Outputs are in UTXO pool
			if (!utxoPool.contains(utxo)) {
				return false;
			}
			// (2) Validate signature
			if (!output.address.verifySignature(getRawDataToSign(i), input.signature)) {
				return false;
			}
			// (3) No multiple UXTO claimed
			if (txPool.contains(utxo)) {
				return false;
			}
			txPool.addUTXO(utxo);
			sumInput += output.value;
		}
		// (4) Outputs are non-nagative
		for (Transaction.Output output : tx.getOutputs()) {
			if (output.value < 0) {
				return false;
			}
			sumOutput += output.value;
		}
		// (5) Input must grater than or equal to output
		if (sumOutput < sumInput) {
			return false;
		}

		return true;
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {

        List<Transaction> validTxs = new ArrayList<Transaction>();

		for (Transaction tx : possibleTxs) {
			if (isValidTx(tx)) {
                validTxs.add(tx);
				//remove mentioned utxo
				for (Transaction.Iutput iutput : tx.getInputs()) {
					UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
					utxoPool.remove(utxo);
				}
				//add new utxo
				for (int i = 0; i < tx.numOutputs(); i++) {
					UTXO utxo = new UTXO(tx.getHash(), i);
					utxoPool.addUTXO(utxo, tx.getOutput(i));
				}
			}
		Transaction[] validTxArray = new Transaction[validTxs.size()];
		return validTxs.toArray(validTxArray);
		}
	} 
}
