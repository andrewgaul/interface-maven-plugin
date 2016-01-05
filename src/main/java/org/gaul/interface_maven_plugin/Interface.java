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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

final class Interface {
    private final Collection<Pattern> exclusions;

    // TODO: should this take Collection<Pattern>?
    Interface(Collection<String> exclusions) {
        Collection<Pattern> patterns = new ArrayList<Pattern>();
        for (String exclusion : exclusions) {
            patterns.add(Utils.globToPattern(exclusion));
        }
        this.exclusions = Utils.createImmutableSet(patterns);
    }

    Collection<ViolationOccurrence> check(ClassReader classReader)
            throws IOException {
        InterfaceClassVisitor classVisitor = new InterfaceClassVisitor(
                exclusions);
        classReader.accept(classVisitor, 0);
        return classVisitor.getOccurrences();
    }

    Collection<ViolationOccurrence> check(InputStream is) throws IOException {
        return check(new ClassReader(is));
    }
}

final class InterfaceClassVisitor extends ClassVisitor {
    // TODO: configure public or protected
    private static final int ACC_PUBLIC_OR_PROTECTED =
            Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED;
    private final Collection<Pattern> exclusions;
    private final Collection<ViolationOccurrence> occurrences =
            new ArrayList<ViolationOccurrence>();
    private boolean publicClass = true;
    private String packageName;

    InterfaceClassVisitor(Collection<Pattern> exclusions) {
        super(Opcodes.ASM5);
        this.exclusions = Utils.checkNotNull(exclusions);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        if ((access & ACC_PUBLIC_OR_PROTECTED) == 0) {
            publicClass = false;
        }
        if (name.contains("/")) {
            packageName = name.substring(0, name.lastIndexOf('/'))
                    .replace('/', '.');
        } else {
            packageName = "";
        }
        for (String itr : interfaces) {
            // TODO: how to get interface Type?
            //checkToken(itr, name, /*lineNumber=*/ -1);
        }
        // TODO: checked exceptions
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        if (publicClass && (access & ACC_PUBLIC_OR_PROTECTED) != 0) {
            Type type = Type.getType(desc);
            checkToken(type, "", -1);
        }
        return super.visitField(access, name, desc, signature, value);
    }

    // TODO: what if we are overriding a method with an external type?
    // TODO: what if we have a public method of a non-public class?
    @Override
    public MethodVisitor visitMethod(int access, final String methodName,
            final String methodDescriptor, final String methodSignature,
            String[] exceptions) {
        if (publicClass && (access & ACC_PUBLIC_OR_PROTECTED) != 0) {
            Type returnType = Type.getReturnType(methodDescriptor);
            checkToken(returnType, "", -1);
            Type[] types = Type.getArgumentTypes(methodDescriptor);
            for (Type type : types) {
                checkToken(type, "", -1);
            }
        }
        return super.visitMethod(access, methodName,
                methodDescriptor, methodSignature, exceptions);
    }

    private void checkToken(Type type, String name, int lineNumber) {
        int sort = type.getSort();
        if (sort != Type.OBJECT) {
            // TODO: arrays?
            return;
        }
        String token = type.getClassName();
        for (Pattern pattern : exclusions) {
            if (pattern.matcher(token).matches()) {
                return;
            }
        }
        occurrences.add(new ViolationOccurrence(name, lineNumber,
                token));
    }

    Collection<ViolationOccurrence> getOccurrences() {
        return occurrences;
    }
}
