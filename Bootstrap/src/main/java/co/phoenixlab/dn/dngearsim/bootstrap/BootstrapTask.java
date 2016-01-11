package co.phoenixlab.dn.dngearsim.bootstrap;

import javafx.concurrent.Task;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.READ;

public class BootstrapTask extends Task<Method> {

    private static final int DIGEST_BUFFER_SIZE = 8192;

    /**
     * Listener to call on successful load
     */
    private final Consumer<Method> onBootstrapOk;

    /**
     * Listener to call when the bootstrapper itself needs to be updated externally
     */
    private final Runnable onBootstrapSelfUpdateRequired;

    /**
     * Whether or not the bootstrapper itself needs to be updated externally
     */
    private volatile boolean selfUpdateRequred;

    /**
     * The MessageDigest using SHA-256 to use to hash
     */
    private final MessageDigest sha256Digest;

    /**
     * Reusable buffer for computing digests
     */
    private final ByteBuffer digestBuffer;

    public BootstrapTask(Consumer<Method> onBootstrapOk, Runnable onBootstrapSelfUpdateRequired) {
        this.onBootstrapOk = onBootstrapOk;
        this.onBootstrapSelfUpdateRequired = onBootstrapSelfUpdateRequired;
        this.selfUpdateRequred = false;
        updateTitle("BootstrapTask");

        try {
            sha256Digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsae) {
            //  This shouldn't happen, since SHA-256 MUST be present on all implementations of Java
            throw new AssertionError("MessageDigest.getInstance(SHA-256) should never throw NoSuchAlgorithmnExcepton");
        }
        digestBuffer = ByteBuffer.allocateDirect(DIGEST_BUFFER_SIZE);
    }

    @Override
    protected Method call() throws Exception {
        //  TODO
        return null;
    }

    @Override
    protected void succeeded() {
        if (selfUpdateRequred) {
            onBootstrapSelfUpdateRequired.run();
        } else {
            onBootstrapOk.accept(getValue());
        }
    }

    @Override
    protected void failed() {

    }

    @Override
    protected void cancelled() {

    }

    private byte[] computeFileHash(Path path) throws IOException {
        sha256Digest.reset();
        digestBuffer.rewind();
        try (ByteChannel channel = Files.newByteChannel(path, READ)) {
            while (channel.read(digestBuffer) > -1) {
                digestBuffer.flip();
                sha256Digest.update(digestBuffer);
            }
        }
        return sha256Digest.digest();
    }
}
