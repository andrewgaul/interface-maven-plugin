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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.objectweb.asm.ClassReader;

public final class InterfaceTest {
    private static final Collection<String> EXCLUDE_JAVA =
            Collections.singleton("java.**");
    private static final Collection<String> EXCLUDE_NOTHING =
            Collections.<String>emptySet();

    // TODO: break into single tests?
    @Test
    public void testArrayListWithoutExclusion() throws Exception {
        ClassReader cr = new ClassReader(ArrayListTestClass.class.getName());
        Collection<ViolationOccurrence> occurrences =
                new Interface(EXCLUDE_NOTHING).check(cr);
        assertThat(occurrences).hasSize(4);
    }

    @Test
    public void testArrayListWithExclusion() throws Exception {
        ClassReader cr = new ClassReader(ArrayListTestClass.class.getName());
        Collection<ViolationOccurrence> occurrences =
                new Interface(EXCLUDE_JAVA).check(cr);
        assertThat(occurrences).hasSize(0);
    }

    // TODO: test exceptions
    // TODO: test interfaces

    public static final class ArrayListTestClass {
        public static final ArrayList<Object> field = null;
        public static ArrayList<Object> methodReturn() {
            return null;
        }
        public static void methodArgument(ArrayList<Object> param) {
        }
        public ArrayListTestClass(ArrayList<Object> param) {
        }
    }
}
