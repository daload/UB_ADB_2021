package org.adbs.dbxic.storage;

import org.adbs.dbxic.utils.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * BufferManager: basic implementation of a buffer manager.
 */
public class BufferManager {
	
    private int nBlocks;
    private Block [] blocks;
    private int currentIndex; // maintain the currently free array position

    private Map<Pair, Integer> idToIdx; // to map a block id to its index in the blocks array
    private Map<Pair, Boolean> idDirty; // to map a block id to know if it is dirty or not
    private LinkedList<Pair> lReplacement; // Replacement queue, to know which block to remove if necessary

    /**
     * Constructor: creates a new buffer given its size in terms of the number of blocks it should handle
     */
    public BufferManager(int nBlocks) {
        
        this.nBlocks = nBlocks;
        blocks = new Block[nBlocks];
        currentIndex = 0;

        idToIdx = new HashMap<Pair, Integer>();
        lReplacement = new LinkedList<Pair>();
        idDirty = new HashMap<Pair, Boolean>();
    } // BufferManager()

    
    /**
     * getNumberOfBlocks: returns the number of blocks stored in the buffer
     */
    public int getNumberOfBlocks() {
        return nBlocks;
    } // getNumberOfBlocks()


    /**
     * containsBlock: returns whether a block is in the buffer or not, looking for the block's identifiers
     */
    public boolean containsBlock(String name, int number) {
        return containsBlock(new Pair(name,number));
    } // containsBlock()


    /**
     * containsBlock: returns whether a block is in the buffer or not, looking for the block's identifiers
     */
    public boolean containsBlock(Pair blockid) {
        return getIndex(blockid) != -1;
    } // containsBlock()

    
    /**
     * getBlock: returns a block given its identifiers, moving it to the back of the replacement queue.
     */
    public Block getBlock(String name, int number) {
        Pair blockid = new Pair(name,number);
        int index = getIndex(blockid);
        if (index >= 0) {  // if it exists, we mark it as recently used by moving it to the back of the replacement queue
            lReplacement.remove(blockid);
            lReplacement.add(blockid);
            return blocks[index];
        }
        return null;
    } // getBlock()


    /**
     * getIndex: returns the index of a block in the buffer given its identifiers.
     */
    protected int getIndex(String name, int number) {
        return getIndex(new Pair(name,number));
    } // getIndex()

    /**
     * getIndex: returns the index of a block in the buffer given a Pair that contains its identifiers.
     * If the block is not in the buffer, it returns -1
     */
    protected int getIndex(Pair blockid) {
        Integer idx = idToIdx.get(blockid);
        return (idx == null ? -1 : idx);
    } // getIndex()


    /**
     * touchBlock: marks a block as dirty and moves it to the back of the replacement queue.
     */
    public void touchBlock(Block block) {
        touchBlock(block.getFileName(),block.getBlockNumber());
    } // touchBlock()


    /**
     * touchBlock: marks a block as dirty and moves it to the back of the replacement queue.
     */
    public void touchBlock(String name, int number) {
        Pair blockid = new Pair(name,number);
        if (containsBlock(blockid)) {
            idDirty.put(blockid, true);
            lReplacement.remove(blockid);
            lReplacement.add(blockid);
        }
    } // touchBlock()


    /**
     * putBlock: includes a block in the buffer. It assumes that the block is dirty
     */
    public Block putBlock(Block block) {
        return putBlock(block, true);
    } // putBlock()


    /**
     * putBlock: includes a block in the buffer.
     */
    public Block putBlock(Block block, boolean dirty) {
        Pair blockid = new Pair(block.getFileName(),block.getBlockNumber());
        int index;
        if (containsBlock(blockid)) { // if the block is already in the buffer
            index = getIndex(blockid);
            blocks[index] = block;      // TODO: this replacement should be checked: when to allow a rewrite?
            idDirty.put(blockid, dirty);
            lReplacement.remove(blockid); // move it to the back of the replacement queue
            lReplacement.add(blockid);
            return null;
        }
        else if (! isFull()) { // if the block is not in the buffer, and this is not full
            index = currentIndex++;
            blocks[index] = block;
            idToIdx.put(blockid, index);
            idDirty.put(blockid, dirty);
            lReplacement.add(blockid);
            return null;
        }
        else { // if the block is not in the buffer, and this is FULL
            index = indexToEvict(); // pull from replacement queue
            Block blockToFlush = blocks[index];
            Pair idBlockToFlush = new Pair(blockToFlush.getFileName(),blockToFlush.getBlockNumber());
            // remove block to flush
            idToIdx.remove(idBlockToFlush);
            idDirty.remove(idBlockToFlush);
            // add new
            idToIdx.put(blockid, index);
            idDirty.put(blockid, dirty);
            blocks[index] = block;
            lReplacement.add(blockid);
            return blockToFlush;
        }
    } // putBlock()

    
    /**
     * isFull: returns whether the buffer is full or not
     */
    protected boolean isFull() {
        return currentIndex == nBlocks;
    } // isFull()

    
    /**
     * indexToEvict: returns the block to be evicted from the buffer to make room.
     */
    protected int indexToEvict() {
        Pair blockid = lReplacement.removeFirst();
        return idToIdx.get(blockid);
    } // indexToEvict()


    /**
     * removeAll: remove all buffer entries for a specific file.
     */
    public void removeAll(String fn) {
        int index = 0;
        while (index < currentIndex) {
            if (blocks[index].getFileName().equals(fn)) { // remove block from buffer
                Pair blockid = new Pair(blocks[index].getFileName(),blocks[index].getBlockNumber());
                System.arraycopy(blocks, index+1, blocks, index,currentIndex-index-1);
                idToIdx.remove(blockid);
                lReplacement.remove(blockid);
                // update all block references in the id to index map
                for (Map.Entry<Pair, Integer> e : idToIdx.entrySet()) {
                    int v = e.getValue();
                    if (v > index) e.setValue(v-1);
                }
                currentIndex--;
            }
            else { // next
                index++;
            }
        }
    } // invalidate()





    /**
     * blocks: returns an iterable over the blocks of the buffer
     */
    Iterable<Block> blocks() {
        return new BlockIteratorWrapper();
    } // blocks()

    /**
     * BlockIteratorWrapper: iterable class for the blocks of the buffer
     */
    class BlockIteratorWrapper implements Iterable<Block> {
        private int index;
        public BlockIteratorWrapper() { index = 0; }

        public Iterator<Block> iterator() {
            return new Iterator<Block>() {
                public boolean hasNext() { return index < currentIndex; }
                public Block next() { return blocks[index++]; }
                public void remove() { throw new UnsupportedOperationException(); }
            }; // new Iterator
        } // iterator()
    } // BlockIteratorWrapper

} // BufferManager
