/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package co.phoenixlab.dn.dngearsim.bootstrap;

import co.phoenixlab.dn.dngearsim.utils.version.VersionHashPair;
import co.phoenixlab.dn.dngearsim.utils.version.Versions;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

import static java.nio.file.StandardOpenOption.READ;

public class BootstrapTask extends Task<BootstrapHandoff> {

    /**
     * Size of the buffer to use to read in the file for digesting
     */
    private static final int DIGEST_BUFFER_SIZE = 8192;

    /**
     * Return result when the version-hashpair can't be retrieved
     */
    private static final VersionHashPair NOT_FOUND = new VersionHashPair();

    /**
     * Return result when the version can't be retrieved
     */
    private static final int NOT_FOUND_I = 0;

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

    /**
     * Handler to call when the task fails, with the failure reason
     */
    private final Consumer<String> onFailed;

    /**
     * The ResourceBundle for localization strings
     */
    private final ResourceBundle localeBundle;

    /**
     * Configuration for this bootstrap task
     */
    private BootstrapConfig config;

    public BootstrapTask(Consumer<BootstrapHandoff> onBootstrapOk, Runnable onBootstrapSelfUpdateRequired,
                         Consumer<String> onFailed, ResourceBundle localeBundle) {
        this.onBootstrapOk = onBootstrapOk;
        this.onBootstrapSelfUpdateRequired = onBootstrapSelfUpdateRequired;
        this.onFailed = onFailed;
        this.localeBundle = localeBundle;
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
        config = new BootstrapConfig();
        if (doesBootstrapRequireUpdate()) {
            selfUpdateRequred = true;
            return null;
        }
        checkAndPerformUpdaterUpdate();


        updateProgress(1D);

        //  TODO
        errorMessage = Optional.of("Handoff not yet implemented");
        throw new Exception();
    }

    private void updateProgress(double d) {
        updateProgress(d, 1D);
    }

    private boolean doesBootstrapRequireUpdate() throws Exception {
        updateMessage(localeBundle.getString("bootstrap.splash.text.checking_bootstrap"));
        TimeUnit.MILLISECONDS.sleep(500);
        int local = getLocalBootstrapVersion();
        int remote = getLatestRemoteBootstrapVersion();
        Bootstrap.LOGGER.info(String.format("Local: %s Remtoe: %s",
                Versions.toString(local), Versions.toString(remote)));
        //  This system does not perform a hashs check because the assumption is that if the bootstrap is not intact
        //  then no guarantee can be made about the running code
        //  Requires update if the remote version is greater than the local
        //  Don't update if we can't get a response from the server
        return remote != NOT_FOUND_I && Versions.compare(remote, local) > 0;
    }

    private int getLocalBootstrapVersion() {
        try (InputStream inputStream = getClass().getResourceAsStream("/co/phoenixlab/dn/dngearsim/bootstrap/version")) {
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter(";");
            String versionStr = scanner.next();
            String compileTime = scanner.next();
            return Versions.parseVersion(versionStr);
        } catch (IOException | NumberFormatException | NoSuchElementException exception) {
            return NOT_FOUND_I;
        }
    }

    private int getLatestRemoteBootstrapVersion() {
        try {
            URL bootstrapVersionUrl = new URL(config.remoteUpdateBaseUrl + "bootstrap/version");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(bootstrapVersionUrl.openStream()))) {
                String vers = reader.readLine();
                return Versions.parseVersion(vers);
            }
        } catch (Exception ignored) {
            Bootstrap.LOGGER.log(Level.WARNING, "Unable to get remote bootstrap version", ignored);
        }
        return NOT_FOUND_I;
    }

    private void checkAndPerformUpdaterUpdate() throws Exception {
        updateMessage(localeBundle.getString("bootstrap.splash.text.checking_updater"));
        TimeUnit.MILLISECONDS.sleep(500);
        VersionHashPair local = getLocalUpdaterVersion();
        VersionHashPair remote = getRemoteUpdaterVersion();
        //  Check if we got a remote version, if not then we obviously cant update
        //  If we don't have an existing updater or the existing updater was corrupted or is an older version, then
        //  perform an update
        //  And if we can't do either, we're screwed
        if (remote == NOT_FOUND && local == NOT_FOUND) {
            errorMessage = Optional.of(localeBundle.getString("bootstrap.splash.error.text.no_updater"));
            throw new Exception();
        }
        if (remote.isVersionHigherThan(local) || (remote.areVersionsEqual(local) && !remote.areHashesEqual(local))) {
            performUpdaterUpdate(remote);
        } else {
            updateMessage(localeBundle.getString("bootstrap.splash.text.updater_ok"));
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private VersionHashPair getLocalUpdaterVersion() {
        //  Find the JAR
        Path jarPath = config.updaterJarPath;
        if (Files.notExists(jarPath)) {
            return NOT_FOUND;
        }
        //  Get the version info next to the JAR
        Path updaterVersionPath = config.updaterBinDir.resolve("VERSION");
        if (Files.notExists(updaterVersionPath)) {
            return NOT_FOUND;
        }
        try {
            byte[] data = Files.readAllBytes(updaterVersionPath);
            String versionData = new String(data, StandardCharsets.UTF_8);
            VersionHashPair pair = VersionHashPair.parse(versionData);
            //  Verify hash
            byte[] hash = computeFileHash(jarPath);
            if (pair.areHashesEqual(new VersionHashPair(0, hash))) {
                return pair;
            }
        } catch (Exception ignored) {
        }
        return NOT_FOUND;
    }

    private VersionHashPair getRemoteUpdaterVersion() {
        try {
            URL patcherVersionURL = new URL(config.remoteUpdateBaseUrl + config.updaterProjectName + "/version");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(patcherVersionURL.openStream()))) {
                String vers = reader.readLine();
                return VersionHashPair.parse(vers);
            }
        } catch (Exception ignored) {
        }
        return NOT_FOUND;
    }

    private void performUpdaterUpdate(VersionHashPair remote) {

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
            return localeBundle.getString("bootstrap.splash.error.text.unexpected_exception") +
                    " " + throwable.getClass().getSimpleName();
        } else {
            return localeBundle.getString("bootstrap.splash.error.text.unexpected");
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
