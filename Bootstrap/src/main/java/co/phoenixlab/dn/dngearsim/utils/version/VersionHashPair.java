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
        return areVersionsEqual(that) &&
                areHashesEqual(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, hash);
    }

    public boolean isVersionHigherThan(VersionHashPair other) {
        return Integer.compareUnsigned(version, other.version) > 0;
    }

    public boolean areHashesEqual(VersionHashPair other) {
        return MessageDigest.isEqual(hash, other.hash);
    }

    public boolean areVersionsEqual(VersionHashPair other) {
        return version == other.version;
    }

    @Override
    public String toString() {
        return Versions.toString(version) + "," + hashToString(hash);
    }

    public static VersionHashPair parse(String s) throws NumberFormatException {
        VersionHashPair pair = new VersionHashPair();
        pair.fromString(s);
        return pair;
    }

    public void fromString(String s) throws NumberFormatException {
        if (s == null || s.isEmpty()) {
            throw new NumberFormatException("empty string");
        }
        String[] split = s.split(",");
        if (split.length != 2) {
            throw new NumberFormatException(s);
        }
        setVersion(Versions.parseVersion(split[0]));
        setHash(hashStringToByteArray(split[1]));
    }

    private static String hashToString(byte[] hash) throws NumberFormatException {
        assert hash.length % 4 == 0 : "Hash size must be multiple of 4";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hash.length; i += 4) {
            builder.append(String.format("%02X", hash[i]));
            builder.append(String.format("%02X", hash[i + 1]));
            builder.append(String.format("%02X", hash[i + 2]));
            builder.append(String.format("%02X", hash[i + 3]));
        }
        return builder.toString();
    }

    private static byte[] hashStringToByteArray(String hashStr) throws NumberFormatException {
        byte[] ret = new byte[32];
        char[] chars = hashStr.toCharArray();
        if (chars.length != 64) {
            throw new NumberFormatException(hashStr);
        }
        for (int i = 0; i < 32; i++) {
            ret[i] = (byte) (hexDigit(chars[i * 2]) << 4);
            ret[i] |= hexDigit(chars[i * 2 + 1]);
        }
        return ret;
    }

    private static int hexDigit(char c) throws NumberFormatException {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            //  equiv to c - 'A' + 10, since 'A' is ASCII #65 and '7' is #55
            return c - '7';
        }
        throw new NumberFormatException("Unknown hex digit character '" + c + "'");
    }
}
