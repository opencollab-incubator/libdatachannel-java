package tel.schich.libdatachannel;

import org.junit.jupiter.api.Test;
import tel.schich.libdatachannel.exception.InvalidException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static tel.schich.libdatachannel.LibDataChannelNative.*;

class TrackLifecycleTest {
    private static final PeerConnectionConfiguration CONFIG =
            PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true);
    private static final String SDP = "m=audio 9 UDP/TLS/RTP/SAVPF 111\r\n"
            + "a=mid:audio\r\na=sendonly\r\na=rtpmap:111 opus/48000/2\r\n";

    @Test
    void peerCloseDeletesLocallyAddedSdpTrack() throws Exception {
        assertPeerDeletesTrack(false);
    }

    @Test
    void peerCloseDeletesLocallyAddedConfiguredTrack() throws Exception {
        assertPeerDeletesTrack(true);
    }

    private static void assertPeerDeletesTrack(boolean configured) throws Exception {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG)) {
            Track track = configured ? peer.addTrack(TrackInit.DEFAULT.withCodec(Track.Codec.RTC_CODEC_OPUS)
                    .withDirection(Track.Direction.RTC_DIRECTION_SENDONLY)) : peer.addTrack(SDP);
            int handle = handle(track);
            try {
                assertTrue(rtcGetTrackDirection(handle) >= 0);
                peer.close();
                assertThrows(InvalidException.class, () -> rtcGetTrackDirection(handle),
                        "native track handle survived peer close");
                assertDoesNotThrow(track::close, "a separately owned wrapper must remain safe to close");
                assertDoesNotThrow(peer::close);
            } finally {
                rtcDeleteTrack(handle); // also clean up on the unfixed implementation
            }
        }
    }

    @Test
    void peerCloseDeletesIncomingTrack() throws Exception {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG)) {
            int handle = rtcAddTrack(peer.peerHandle, SDP);
            assertTrue(handle >= 0);
            try {
                peer.listener.onTrack(handle);
                peer.close();
                assertThrows(InvalidException.class, () -> rtcGetTrackDirection(handle));
            } finally {
                rtcDeleteTrack(handle);
            }
        }
    }

    @Test
    void explicitTrackCloseIsIdempotentAndPeerCanCloseAfterIt() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG)) {
            Track track = peer.addTrack(SDP);
            track.close();
            assertDoesNotThrow(track::close);
        }
    }

    @Test
    void incomingTrackPublishedAfterPeerCloseIsDeletedInsteadOfDelivered() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG)) {
            int handle = rtcAddTrack(peer.peerHandle, SDP);
            assertTrue(handle >= 0);
            try {
                peer.close();
                // Models an incoming track allocated before close but delivered after its snapshot.
                peer.listener.onTrack(handle);
                assertThrows(InvalidException.class, () -> rtcGetTrackDirection(handle));
            } finally {
                rtcDeleteTrack(handle);
            }
        }
    }

    private static int handle(Track track) throws Exception {
        Field field = Track.class.getDeclaredField("trackHandle");
        field.setAccessible(true);
        return field.getInt(track);
    }
}
