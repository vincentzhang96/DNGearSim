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

import java.nio.file.Path;
import java.nio.file.Paths;

public class BootstrapConfig {

    public final Path binaryDir;
    public final Path appConfigDir;
    public final Path dnDataCacheDir;
    public final Path jreDir;
    public final Path logDir;

    public final Path updaterBinDir;
    public final Path updaterJarPath;

    public final String remoteUpdateBaseUrl;
    public final String updaterProjectName;

    public BootstrapConfig() {
        binaryDir = Paths.get(System.getProperty("dngs.bootstrap.bindir", "bin"));
        appConfigDir = Paths.get(System.getProperty("dngs.bootstrap.appconfigdir", "appconfig"));
        dnDataCacheDir = Paths.get(System.getProperty("dngs.bootstrap.dndatacachedir", "dndatacache"));
        jreDir = Paths.get(System.getProperty("dngs.bootstrap.jredir", "jre"));
        logDir = Paths.get(System.getProperty("dngs.bootstrap.logdir", "log"));

        updaterProjectName = System.getProperty("dngs.bootstrap.updatername", "updater");
        updaterBinDir = binaryDir.resolve(updaterProjectName);
        updaterJarPath = updaterBinDir.resolve(updaterProjectName + ".jar");

        remoteUpdateBaseUrl = System.getProperty("dngs.bootstrap.remoteUrl",
                "http://ilithyia.phoenixlab.co/dn/gearsim/");
    }


}
