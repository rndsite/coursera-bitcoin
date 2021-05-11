// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.*;

public class BlockChain {
    public class BlockNode {
        public Block block;
        public BlockNode parent;
        public List<BlockNode> children;
        public int height;
        public UTXOPool utxoPool;

        public BlockNode(BlockNode parent, Block block, UTXOPool utxoPool) {
            this.block = block;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.utxoPool = new UTXOPool(utxoPool);
            if (parent != null) {
                parent.children.add(this);
                this.height = parent.height + 1;
            } else {
                this.height = 1;
            }
        }
    }

    public static final int CUT_OFF_AGE = 10;

    private TransactionPool txPool;
    private BlockNode maxHeightNode;
    private Map<byte[], BlockNode> hashBlockMap;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS

        this.txPool = new TransactionPool();
        this.hashBlockMap = new HashMap<>();

        UTXOPool utxoPool = new UTXOPool();
        Transaction coinbase = genesisBlock.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            utxoPool.addUTXO(new UTXO(coinbase.getHash(), i), coinbase.getOutput(i));
        }

        this.maxHeightNode = new BlockNode(null, genesisBlock, utxoPool);
        this.hashBlockMap.put(genesisBlock.getHash(), this.maxHeightNode);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return this.maxHeightNode.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return new UTXOPool(this.maxHeightNode.utxoPool);
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return new TransactionPool(txPool);
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        byte[] prevBlockHash = block.getPrevBlockHash();
        if (prevBlockHash == null) {
            return false;
        }

        if (!this.hashBlockMap.containsKey(prevBlockHash)) {
            return false;
        }

        BlockNode parent = this.hashBlockMap.get(prevBlockHash);
        int height = parent.height + 1;
        if (height <= (this.maxHeightNode.height - this.CUT_OFF_AGE)) {
            return false;
        }

        TxHandler txHandler = new TxHandler(parent.utxoPool);
        Transaction[] txs =  block.getTransactions().toArray(new Transaction[0]);
        Transaction[] validTxs = txHandler.handleTxs(txs);
        if (txs.length != validTxs.length) {
            return false;
        }

        UTXOPool utxoPool = txHandler.getUTXOPool();
        Transaction coinbase = block.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            utxoPool.addUTXO(new UTXO(coinbase.getHash(), i), coinbase.getOutput(i));
        }

        BlockNode node = new BlockNode(parent, block, utxoPool);
        this.hashBlockMap.put(block.getHash(), node);
        if (height > this.maxHeightNode.height) {
            this.maxHeightNode = node;
        }
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        txPool.addTransaction(tx);
    }
}