package org.itmo;

import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicBitmap {
    private final int bitCount;
    private final AtomicLongArray array;

    public AtomicBitmap(int bitCount) {
        this.bitCount = bitCount;
        int bucketCount = (bitCount + 63) >>> 6;
        array = new AtomicLongArray(bucketCount);
    }

    public void setBit(int bit) {
        int bucketIdx = bit >>> 6;
        long mask = 1L << (bit & 63);
        array.getAndUpdate(bucketIdx, val -> val | mask);
    }

    public boolean setBitCAS(int bit) {
        int bucketIdx = bit >>> 6;
        long mask = 1L << (bit & 63);
        long bucket;
        while (true) {
            bucket = array.get(bucketIdx);
            if ((bucket & mask) != 0) {
                return false;
            }
            if (array.compareAndSet(bucketIdx, bucket, bucket | mask)) {
                return true;
            }
        }
    }

    public boolean getBit(int bit) {
        int bucketIdx = bit >>> 6;
        long mask = 1L << (bit & 63);
        return (array.get(bucketIdx) & mask) != 0;
    }

    public long getBucket(int bucketIdx) {
        return array.get(bucketIdx);
    }

    public boolean[] toBooleanArray() {
        boolean[] booleanArray = new boolean[bitCount];
        for (int i = 0; i < bitCount; i++) {
            booleanArray[i] = getBit(i);
        }
        return booleanArray;
    }
}
