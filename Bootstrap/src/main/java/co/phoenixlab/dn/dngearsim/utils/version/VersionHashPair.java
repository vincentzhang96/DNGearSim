package co.phoenixlab.dn.dngearsim.utils.version;

import java.security.MessageDigest;
import java.util.Objects;

public class VersionHashPair {

    private static final byte[] EMPTY_DIGEST = new byte[32];

    /**
     * Version number, to be interpreted using
     */
    private int version;
    private byte[] hash;

    public VersionHashPair() {
        version = 0;
        hash = EMPTY_DIGEST;
    }

    public VersionHashPair(int version, byte[] hash) {
        this.version = version;
        this.hash = hash;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VersionHashPair that = (VersionHashPair) o;
        return version == that.version &&
                MessageDigest.isEqual(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, hash);
    }

    public boolean isVersionHigherThan(VersionHashPair other) {
        return Integer.compareUnsigned(version, other.version) > 0;
    }

}
