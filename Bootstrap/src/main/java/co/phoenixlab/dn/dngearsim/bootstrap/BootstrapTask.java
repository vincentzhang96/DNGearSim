package co.phoenixlab.dn.dngearsim.bootstrap;

import co.phoenixlab.dn.dngearsim.utils.version.VersionHashPair;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.READ;

public class BootstrapTask extends Task<BootstrapHandoff> {

    /**
     * Size of the buffer to use to read in the file for digesting
     */
    private static final int DIGEST_BUFFER_SIZE = 8192;

    private static final VersionHashPair NOT_FOUND = new VersionHashPair();

    /**
     * Listener to call on successful load
     */
    private final Consumer<BootstrapHandoff> onBootstrapOk;

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

    /**
     * A custom error message to display on exceptions
     */
    private Optional<String> errorMessage;

    private final Consumer<String> onFailed;

    public BootstrapTask(Consumer<BootstrapHandoff> onBootstrapOk, Runnable onBootstrapSelfUpdateRequired,
                         Consumer<String> onFailed) {
        this.onBootstrapOk = onBootstrapOk;
        this.onBootstrapSelfUpdateRequired = onBootstrapSelfUpdateRequired;
        this.onFailed = onFailed;
        this.errorMessage = Optional.empty();
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
    protected BootstrapHandoff call() throws Exception {
        checkAndPerformLauncherUpdate();


        //  TODO
        return null;
    }

    private void checkAndPerformLauncherUpdate() throws Exception {
        updateMessage("Checking updater version");
        TimeUnit.MILLISECONDS.sleep(500);
        VersionHashPair local = getLocalUpdaterVersion();
        VersionHashPair remote = getRemoteUpdaterVersion();
        //  Check if we got a remote version, if not then we obviously cant update
        //  If we don't have an existing updater or the existing updater was corrupted or is an older version, then
        //  perform an update
        //  And if we can't do either, we're screwed
        if (remote == NOT_FOUND && local == NOT_FOUND) {
            errorMessage = Optional.of("Launcher is missing and could not be downloaded\n" +
                    "Please check your connection and try again");
            throw new Exception();
        }
        if (remote.isVersionHigherThan(local)  || (remote.areVersionsEqual(local) && !remote.areHashesEqual(local))) {
            performUpdate(remote);
        } else {
            updateMessage("Updater check OK");
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private VersionHashPair getLocalUpdaterVersion() {

        //  TODO
        return NOT_FOUND;
    }

    private VersionHashPair getRemoteUpdaterVersion() {

        //  TODO
        return NOT_FOUND;
    }

    private void performUpdate(VersionHashPair remote) {

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
        onFailed.accept(errorMessage.orElseGet(this::getExceptionErrorMessage));
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private String getExceptionErrorMessage() {
        Throwable throwable = getException();
        if (throwable != null) {
            return "Unexpected error: " + throwable.getClass().getSimpleName();
        } else {
            return "Unknown error";
        }
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
