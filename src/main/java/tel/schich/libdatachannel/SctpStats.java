package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.time.Duration;

/** What a peer's SCTP association reported about itself when it was read. */
public final class SctpStats {
    private final Duration rtt, rto;
    private final long congestionWindow, peerReceiveWindow, unackedChunks, pendingChunks, dataTimeouts,
        sendQueueBytes, sendBufferSize, receiveQueueBytes, fastRetransmits, retransmittedChunks, gapSacks,
        duplicateTsnsReceived;

    private SctpStats(Duration rtt, Duration rto, long congestionWindow, long peerReceiveWindow,
                      long unackedChunks, long pendingChunks, long dataTimeouts, long sendQueueBytes,
                      long sendBufferSize, long receiveQueueBytes, long fastRetransmits,
                      long retransmittedChunks, long gapSacks, long duplicateTsnsReceived) {
        this.rtt = rtt;
        this.rto = rto;
        this.congestionWindow = congestionWindow;
        this.peerReceiveWindow = peerReceiveWindow;
        this.unackedChunks = unackedChunks;
        this.pendingChunks = pendingChunks;
        this.dataTimeouts = dataTimeouts;
        this.sendQueueBytes = sendQueueBytes;
        this.sendBufferSize = sendBufferSize;
        this.receiveQueueBytes = receiveQueueBytes;
        this.fastRetransmits = fastRetransmits;
        this.retransmittedChunks = retransmittedChunks;
        this.gapSacks = gapSacks;
        this.duplicateTsnsReceived = duplicateTsnsReceived;
    }

    @JNIAccess
    static SctpStats create(int rttMillis, int rtoMillis, long congestionWindow, long peerReceiveWindow,
                            long unackedChunks, long pendingChunks, long dataTimeouts, long sendQueueBytes,
                            long sendBufferSize, long receiveQueueBytes, long fastRetransmits,
                            long retransmittedChunks, long gapSacks, long duplicateTsnsReceived) {
        return new SctpStats(Duration.ofMillis(rttMillis), Duration.ofMillis(rtoMillis), congestionWindow,
            peerReceiveWindow, unackedChunks, pendingChunks, dataTimeouts, sendQueueBytes, sendBufferSize,
            receiveQueueBytes, fastRetransmits, retransmittedChunks, gapSacks, duplicateTsnsReceived);
    }

    /** Smoothed round trip time in whole milliseconds, zero until sampled or while under one. */
    public Duration rtt() { return rtt; }
    /** Retransmission timeout, which grows as acknowledgements go missing. */
    public Duration rto() { return rto; }
    /** Congestion window in bytes. */
    public long congestionWindow() { return congestionWindow; }
    /** Receive window the peer last advertised, in bytes. */
    public long peerReceiveWindow() { return peerReceiveWindow; }
    /** DATA chunks sent and not acknowledged yet. */
    public long unackedChunks() { return unackedChunks; }
    /** Received chunks waiting for reassembly or ordering. */
    public long pendingChunks() { return pendingChunks; }
    /** Retransmission timer expiries since the association started, each a stall while data is resent. */
    public long dataTimeouts() { return dataTimeouts; }
    /** Bytes accepted for sending and not acknowledged yet, sent or not, which the buffered amount does not count. */
    public long sendQueueBytes() { return sendQueueBytes; }
    /** Bytes the send queue may hold before messages wait in the buffered amount instead. */
    public long sendBufferSize() { return sendBufferSize; }
    /** Received bytes waiting for reassembly or ordering. */
    public long receiveQueueBytes() { return receiveQueueBytes; }
    /**
     * Packets sent by fast retransmit, after the peer reported gaps. Each halves the congestion window, so where
     * these run ahead of what the path lost, the peer is reporting gaps for data that arrived.
     */
    public long fastRetransmits() { return fastRetransmits; }
    /** DATA chunks sent again, by fast retransmit or after a retransmission timeout. */
    public long retransmittedChunks() { return retransmittedChunks; }
    /** SACKs received that reported gaps in what the peer had. */
    public long gapSacks() { return gapSacks; }
    /** DATA chunks received that had arrived already, so the peer resent what it need not have. */
    public long duplicateTsnsReceived() { return duplicateTsnsReceived; }

    @Override
    public String toString() {
        return "SctpStats{rtt=" + rtt.toMillis() + "ms, rto=" + rto.toMillis() + "ms, congestionWindow=" +
            congestionWindow + ", peerReceiveWindow=" + peerReceiveWindow + ", unackedChunks=" + unackedChunks +
            ", pendingChunks=" + pendingChunks + ", dataTimeouts=" + dataTimeouts + ", sendQueueBytes=" +
            sendQueueBytes + ", sendBufferSize=" + sendBufferSize + ", receiveQueueBytes=" + receiveQueueBytes +
            ", fastRetransmits=" + fastRetransmits + ", retransmittedChunks=" + retransmittedChunks + ", gapSacks=" +
            gapSacks + ", duplicateTsnsReceived=" + duplicateTsnsReceived + "}";
    }
}
