package tel.schich.libdatachannel;

import org.junit.jupiter.api.Test;
import tel.schich.libdatachannel.exception.InvalidException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static tel.schich.libdatachannel.LibDataChannelNative.*;
import static tel.schich.libdatachannel.exception.LibDataChannelException.ERR_INVALID;

class PeerResourceLifecycleTest {
    private static final PeerConnectionConfiguration CONFIG =
            PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true);

    @Test
    void incomingDataChannelPublishedAfterCloseIsDeleted() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG)) {
            DataChannel pending = peer.createDataChannel("late");
            // The native handle exists, but its incoming callback has not handed it to Java yet.
            peer.dropChannelState(pending.channelHandle);
            try {
                peer.close();
                peer.listener.onDataChannel(pending.channelHandle);
                assertEquals(ERR_INVALID, rtcDeleteDataChannel(pending.channelHandle));
            } finally {
                pending.close();
            }
        }
    }

    @Test
    void asynchronousPeerCloseAlsoDeletesTracks() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG)) {
            int handle = rtcAddTrack(peer.peerHandle, "m=audio 9 UDP/TLS/RTP/SAVPF 111\r\n"
                    + "a=mid:audio\r\na=sendonly\r\na=rtpmap:111 opus/48000/2\r\n");
            assertTrue(handle >= 0);
            try {
                peer.listener.onTrack(handle);
                assertTrue(peer.closeAndAwait(Duration.ofSeconds(5)));
                assertThrows(InvalidException.class, () -> rtcGetTrackDirection(handle));
            } finally {
                rtcDeleteTrack(handle);
            }
        }
    }
}
