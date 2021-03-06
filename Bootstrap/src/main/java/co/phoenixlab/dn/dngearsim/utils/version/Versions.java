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

package co.phoenixlab.dn.dngearsim.utils.version;

import java.util.regex.Pattern;

public class Versions {

    public static final String VERSION_ZERO = "0.0.0.0";
    public static final Pattern VERSION_STR_PATTERN = Pattern.compile(
            "(([1-2]?)[0-9]?)[0-9]\\.(([1-2]?)[0-9]?)[0-9]\\.(([1-2]?)[0-9]?)[0-9]\\.(([1-2]?)[0-9]?)[0-9]");

    //  Private constructor, utility class
    private Versions() {
    }

    public static int valueOf(String s) throws NumberFormatException {
        return parseVersion(s);
    }

    public static int parseVersion(String versionStr) throws NumberFormatException {
        if (versionStr == null || versionStr.isEmpty()) {
            throw new NumberFormatException("empty string");
        }
        if (!VERSION_STR_PATTERN.matcher(versionStr).matches()) {
            throw new NumberFormatException(versionStr);
        }
        String[] split = versionStr.split("\\.");
        assert split.length == 4 : "Regex should have caught invalid format";
        int ver = fastParseVersionInt(split[0]) << 24;
        ver |= fastParseVersionInt(split[1]) << 16;
        ver |= fastParseVersionInt(split[2]) << 8;
        ver |= fastParseVersionInt(split[3]);
        return ver;
    }

    /**
     * Parses a version number segment, without performing checks besides a bounds check
     * @param s
     * @return
     * @throws NumberFormatException
     */
    private static int fastParseVersionInt(String s) throws NumberFormatException {
        assert s != null;
        assert s.length() < 4 && s.length() > 0 : "Regex and split should have caught invalid format";
        char[] chars = new char[] {'0', '0', '0'};
        char[] in = s.toCharArray();
        System.arraycopy(in, 0, chars, 3 - in.length, in.length);
        int ret = 0;
        ret += (chars[0] - '0') * 100;
        ret += (chars[1] - '0') * 10;
        ret += chars[2] - '0';
        if (ret > 255) {
            throw new NumberFormatException(s + " out of bound");
        }
        return ret;
    }

    public static String toString(int version) {
        return String.format("%d.%d.%d.%d",
                (version >>> 24),
                (version >> 16) & 0xFF,
                (version >> 8) & 0xFF,
                version & 0xFF);
    }

    public static int compare(int a, int b) {
        return Integer.compareUnsigned(a, b);
    }

}
