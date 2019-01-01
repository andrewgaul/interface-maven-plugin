/*
 * Copyright 2016 Andrew Gaul <andrew@gaul.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gaul.interface_maven_plugin;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class Utils {
    static final Charset UTF_8 = Charset.forName("UTF-8");

    static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    static <T> Set<T> createImmutableSet(Collection<T> collection) {
        return Collections.unmodifiableSet(new HashSet<T>(
                Utils.checkNotNull(collection)));
    }

    static <T, U> Map<T, U> createImmutableMap(Map<T, U> map) {
        return Collections.unmodifiableMap(new HashMap<T, U>(
                Utils.checkNotNull(map)));
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ioe) {
            // swallow exception
        }
    }

    static Collection<String> readAllLines(InputStream is) throws IOException {
        Collection<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is,
                UTF_8));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            lines.add(line);
        }
        return lines;
    }

    private Utils() {
        throw new AssertionError("Intentionally not implemented");
    }

    /**
     * Convert globs to regular expressions.  Examples:
     *
     * java.util.Collection -> "^java\.util\.Collection"
     * java.util.* -> "^java\.util\.[^.]*$"
     * java.util.** -> "^java\.util\..*$"
     */
    static Pattern globToPattern(String glob) {
        StringBuilder builder = new StringBuilder();
        builder.append("^");
        for (int i = 0; i < glob.length(); ++i) {
            char ch = glob.charAt(i);
            switch (ch) {
            case '\\':
                builder.append("\\\\");
                break;
            case '.':
                builder.append("\\.");
                break;
            case '*':
                if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                    builder.append(".*");
                    ++i;
                } else {
                    builder.append("[^.]*");
                }
                break;
            default:
                builder.append(ch);
                break;
            }
        }
        builder.append("$");
        return Pattern.compile(builder.toString());
    }
}
