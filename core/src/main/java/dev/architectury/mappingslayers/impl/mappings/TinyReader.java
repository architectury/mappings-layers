/*
 * Copyright 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.architectury.mappingslayers.impl.mappings;

import dev.architectury.mappingslayers.api.MappingsEntryType;
import dev.architectury.mappingslayers.api.mutable.*;
import dev.architectury.mappingslayers.api.utils.MappingsUtils;
import dev.architectury.mappingslayers.impl.tiny.MappedImpl;
import dev.architectury.mappingslayers.impl.tiny.TinyTreeImpl;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Copy pasta of {@link net.fabricmc.mapping.reader.v2.TinyV2Factory}
 */
public final class TinyReader {
    public static MutableTinyTree load(BufferedReader reader) throws IOException {
        return load(reader, false);
    }
    
    public static MutableTinyTree load(BufferedReader reader, boolean slim) throws IOException {
        Visitor visitor = new Visitor(slim);
        visit(reader, visitor);
        return visitor.tree;
    }
    
    public static MutableTinyTree loadWithDetection(BufferedReader reader) throws IOException {
        return loadWithDetection(reader, false);
    }
    
    private static void visit(BufferedReader reader, Visitor visitor) throws IOException {
        String line;
        final int namespaceCount;
        final boolean escapedNames;
        try {
            final TinyMetadata meta = readMetadata(reader);
            namespaceCount = meta.getNamespaces().size();
            escapedNames = meta.getProperties().containsKey("escaped-names");
            visitor.start(meta);
            line = reader.readLine();
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Error in the header!", ex);
        }
        
        int lastIndent = -1;
        final TinyState[] stack = new TinyState[4]; // max depth 4
        for (; line != null; line = reader.readLine()) {
            try {
                int currentIndent = countIndent(line);
                if (currentIndent > lastIndent + 1)
                    throw new IllegalArgumentException("Broken indent! Maximum " + (lastIndent + 1) + ", actual " + currentIndent);
                if (currentIndent <= lastIndent) {
                    visitor.pop(lastIndent - currentIndent + 1);
                }
                lastIndent = currentIndent;
                
                final String[] parts = line.split("\t", -1);
                final TinyState currentState = TinyState.get(currentIndent, parts[currentIndent]);
                
                if (!currentState.checkPartCount(currentIndent, parts.length, namespaceCount)) {
                    throw new IllegalArgumentException("Wrong number of parts for definition of a " + currentState + "!");
                }
                
                if (!currentState.checkStack(stack, currentIndent)) {
                    throw new IllegalStateException("Invalid stack " + Arrays.toString(stack) + " for a " + currentState + " at position" + currentIndent + "!");
                }
                
                stack[currentIndent] = currentState;
                
                currentState.visit(visitor, parts, currentIndent, escapedNames);
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Error on line \"" + line + "\"!", ex);
            }
        }
        
        if (lastIndent > -1) {
            visitor.pop(lastIndent + 1);
        }
    }
    
    public static TinyMetadata readMetadata(final BufferedReader reader) throws IOException, IllegalArgumentException {
        final String firstLine = reader.readLine();
        if (firstLine == null)
            throw new IllegalArgumentException("Empty reader!");
        final String[] parts = firstLine.split("\t", -1);
        if (parts.length < 4 || !parts[0].equals("tiny")) {
            throw new IllegalArgumentException("Unsupported format!");
        }
        
        final int majorVersion;
        try {
            majorVersion = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid major version!", ex);
        }
        final int minorVersion;
        try {
            minorVersion = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid minor version!", ex);
        }
        
        final Map<String, String> properties = new LinkedHashMap<>();
        String line;
        reader.mark(8192);
        while ((line = reader.readLine()) != null) {
            switch (countIndent(line)) {
                case 0: {
                    reader.reset();
                    return makeHeader(majorVersion, minorVersion, parts, properties);
                }
                case 1: {
                    String[] elements = line.split("\t", -1); // Care about "" values
                    properties.put(elements[1], elements.length == 2 ? null : elements[2]);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid indent in header! Encountered \"" + line + "\"!");
                }
            }
            reader.mark(8192);
        }
        
        return makeHeader(majorVersion, minorVersion, parts, properties);
    }
    
    private static MutableTinyMetadata makeHeader(int major, int minor, String[] parts, Map<String, String> props) {
        List<String> list = new ArrayList<>(Arrays.asList(parts).subList(3, parts.length));
        return MutableTinyMetadata.create(major, minor, Collections.unmodifiableList(list), Collections.unmodifiableMap(new HashMap<>(props)));
    }
    
    private static int countIndent(String st) {
        final int len = st.length();
        int ret = 0;
        while (ret < len && st.charAt(ret) == '\t') {
            ret++;
        }
        return ret;
    }
    
    public static MutableTinyTree loadWithDetection(BufferedReader reader, boolean slim) throws IOException {
        reader.mark(8192);
        String firstLine = reader.readLine();
        String[] header = firstLine.split("\t");
        reader.reset();
        switch (header[0]) {
            case "tiny":
                return load(reader, slim);
            case "v1":
                return loadLegacy(reader);
        }
        throw new UnsupportedOperationException("Unsupported format with header \"" + firstLine + "\"!");
    }
    
    public static MutableTinyTree loadLegacy(BufferedReader reader) throws IOException {
        String[] header = reader.readLine().split("\t");
        if (header.length <= 1 || !header[0].equals("v1")) {
            throw new IOException("Invalid mapping version!");
        }
        
        String[] namespaceList = new String[header.length - 1];
        System.arraycopy(header, 1, namespaceList, 0, header.length - 1);
        MutableTinyTree tree = MappingsUtils.create(MutableTinyMetadata.create(1, 0, Arrays.asList(namespaceList), new HashMap<>()));
        
        final Map<String, MutableClassDef> firstNamespaceClassEntries = new HashMap<>();
        List<String[]> fieldLines = new ArrayList<>();
        List<String[]> methodLines = new ArrayList<>();
        
        String line;
        while ((line = reader.readLine()) != null) {
            String[] splitLine = line.split("\t");
            if (splitLine.length >= 2) {
                switch (splitLine[0]) {
                    case "CLASS":
                        MutableClassDef entry = tree.getOrCreateClass(splitLine[1]);
                        for (int i = 2; i < splitLine.length; i++) {
                            entry.setName(i - 1, splitLine[i]);
                        }
                        firstNamespaceClassEntries.put(entry.getName(0), entry);
                        break;
                    case "FIELD":
                        fieldLines.add(splitLine);
                        break;
                    case "METHOD":
                        methodLines.add(splitLine);
                        break;
                }
            }
        }
        
        for (String[] splitLine : fieldLines) {
            // FIELD ClassName desc names ...
            String className = splitLine[1];
            MutableClassDef parent = firstNamespaceClassEntries.get(className);
            if (parent == null) {
                parent = tree.getOrCreateClass(className); // No class for my field, sad!
                firstNamespaceClassEntries.put(className, parent);
            }
            
            MutableFieldDef field = parent.getOrCreateField(splitLine[3], splitLine[2]);
            for (int i = 3; i < splitLine.length; i++) {
                field.setName(i - 2, splitLine[i]);
            }
        }
        
        for (String[] splitLine : methodLines) {
            // METHOD ClassName desc names ...
            String className = splitLine[1];
            MutableClassDef parent = firstNamespaceClassEntries.get(className);
            if (parent == null) {
                parent = tree.getOrCreateClass(className); // No class for my field, sad!
                firstNamespaceClassEntries.put(className, parent);
            }
            
            MutableMethodDef method = parent.getOrCreateMethod(splitLine[3], splitLine[2]);
            for (int i = 3; i < splitLine.length; i++) {
                method.setName(i - 2, splitLine[i]);
            }
        }
        
        return tree;
    }
    
    private TinyReader() {
    }
    
    private static final class Visitor {
        private MutableMapped parameterDummy;
        private MutableMapped localsDummy;
        private final boolean slim;
        private @MonotonicNonNull TinyTreeImpl tree;
        private final Deque<MutableMapped> stack = new ArrayDeque<>(4);
        private boolean pushedComment = false;
        private @MonotonicNonNull MutableClassDef inClass = null;
        private @MonotonicNonNull MutableMethodDef inMethod = null;
        
        Visitor(boolean slim) {
            this.slim = slim;
        }
        
        public void start(TinyMetadata metadata) {
            this.tree = new TinyTreeImpl(metadata, Stream.empty());
            this.parameterDummy = new MappedImpl(tree, new String[0], null) {
                @Override
                public MappingsEntryType getType() {
                    return MappingsEntryType.PARAMETER;
                }
            };
            this.localsDummy = new MappedImpl(tree, new String[0], null) {
                @Override
                public MappingsEntryType getType() {
                    return MappingsEntryType.LOCAL_VARIABLE;
                }
            };
        }
        
        public void pushClass(PartGetter name) {
            MutableClassDef clz = this.tree.getOrCreateClass(name.getRawNames()[0]);
            for (int i = 1; i < name.getRawNames().length; i++) {
                clz.setName(i, name.getRawNames()[i]);
            }
            inClass = clz;
            stack.addLast(clz);
        }
        
        public void pushField(PartGetter name, String descriptor) {
            if (inClass == null)
                throw new IllegalStateException();
            
            MutableFieldDef field = inClass.getOrCreateField(name.getRawNames()[0], descriptor);
            for (int i = 1; i < name.getRawNames().length; i++) {
                field.setName(i, name.getRawNames()[i]);
            }
            stack.addLast(field);
        }
        
        public void pushMethod(PartGetter name, String descriptor) {
            if (inClass == null)
                throw new IllegalStateException();
            
            MutableMethodDef method = inClass.getOrCreateMethod(name.getRawNames()[0], descriptor);
            for (int i = 1; i < name.getRawNames().length; i++) {
                method.setName(i, name.getRawNames()[i]);
            }
            inMethod = method;
            stack.addLast(method);
        }
        
        public void pushParameter(PartGetter name, int localVariableIndex) {
            if (inMethod == null) {
                throw new IllegalStateException();
            }
            
            if (slim) {
                stack.addLast(parameterDummy);
                return;
            }
            
            MutableParameterDef par = inMethod.getOrCreateParameter(localVariableIndex, name.getRawNames()[0]);
            for (int i = 1; i < name.getRawNames().length; i++) {
                par.setName(i, name.getRawNames()[i]);
            }
            stack.addLast(par);
        }
        
        public void pushLocalVariable(PartGetter name, int localVariableIndex, int localVariableStartOffset, int localVariableTableIndex) {
            if (inMethod == null) {
                throw new IllegalStateException();
            }
            
            stack.addLast(localsDummy);
            
           /* if (slim) {
                stack.addLast(localsDummy);
                return;
            }
            
            LocalVariableImpl var = inMethod.getLocalVariables(namespaceMapper, name.getRawNames(), localVariableIndex, localVariableStartOffset, localVariableTableIndex);
            inMethod.localVariables.add(var);
            stack.addLast(var);*/
        }
        
        public void pushComment(String comment) {
            if (stack.isEmpty()) {
                throw new IllegalStateException("Nothing to append comment on!");
            }
            
            
            if (pushedComment) {
                throw new IllegalStateException("Commenting on a comment!");
            }
            
            if (!slim) {
                stack.peekLast().setComment(comment);
            }
            pushedComment = true;
        }
        
        public void pop(int count) {
            if (pushedComment) {
                pushedComment = false;
                count--;
            }
            for (int i = 0; i < count; i++) {
                stack.removeLast();
            }
        }
    }
    
    private enum TinyState {
        // c names...
        CLASS(1) {
            @Override
            boolean checkStack(TinyState[] stack, int currentIndent) {
                return currentIndent == 0;
            }
            
            @Override
            void visit(Visitor visitor, String[] parts, int indent, boolean escapedStrings) {
                visitor.pushClass(makeGetter(parts, indent, escapedStrings));
            }
        },
        // f desc names...
        FIELD(2) {
            @Override
            boolean checkStack(TinyState[] stack, int currentIndent) {
                return currentIndent == 1 && stack[currentIndent - 1] == CLASS;
            }
            
            @Override
            void visit(Visitor visitor, String[] parts, int indent, boolean escapedStrings) {
                visitor.pushField(makeGetter(parts, indent, escapedStrings), unescapeOpt(parts[indent + 1], escapedStrings));
            }
        },
        // m desc names...
        METHOD(2) {
            @Override
            boolean checkStack(TinyState[] stack, int currentIndent) {
                return currentIndent == 1 && stack[currentIndent - 1] == CLASS;
            }
            
            @Override
            void visit(Visitor visitor, String[] parts, int indent, boolean escapedStrings) {
                visitor.pushMethod(makeGetter(parts, indent, escapedStrings), unescapeOpt(parts[indent + 1], escapedStrings));
            }
        },
        // p lvIndex names...
        PARAMETER(2) {
            @Override
            boolean checkStack(TinyState[] stack, int currentIndent) {
                return currentIndent == 2 && stack[currentIndent - 1] == METHOD;
            }
            
            @Override
            void visit(Visitor visitor, String[] parts, int indent, boolean escapedStrings) {
                visitor.pushParameter(makeGetter(parts, indent, escapedStrings), Integer.parseInt(parts[indent + 1]));
            }
        },
        // v lvIndex lvStartOffset lvtIndex names...
        LOCAL_VARIABLE(4) {
            @Override
            boolean checkStack(TinyState[] stack, int currentIndent) {
                return currentIndent == 2 && stack[currentIndent - 1] == METHOD;
            }
            
            @Override
            void visit(Visitor visitor, String[] parts, int indent, boolean escapedStrings) {
                visitor.pushLocalVariable(makeGetter(parts, indent, escapedStrings), Integer.parseInt(parts[indent + 1]), Integer.parseInt(parts[indent + 2]), Integer.parseInt(parts[indent + 3]));
            }
        },
        // c comment
        COMMENT(2, false) {
            @Override
            boolean checkStack(TinyState[] stack, int currentIndent) {
                if (currentIndent == 0)
                    return false;
                switch (stack[currentIndent - 1]) {
                    case CLASS:
                    case METHOD:
                    case FIELD:
                    case PARAMETER:
                    case LOCAL_VARIABLE:
                        // Use a whitelist
                        return true;
                    default:
                        return false;
                }
            }
            
            @Override
            void visit(Visitor visitor, String[] parts, int indent, boolean escapedStrings) {
                visitor.pushComment(unescape(parts[indent + 1]));
            }
        };
        
        final int actualParts;
        final boolean namespaced;
        
        TinyState(int actualParts) {
            this(actualParts, true);
        }
        
        TinyState(int actualParts, boolean namespaced) {
            this.actualParts = actualParts;
            this.namespaced = namespaced;
        }
        
        static TinyState get(int indent, String identifier) {
            switch (identifier) {
                case "c":
                    return indent == 0 ? CLASS : COMMENT;
                case "m":
                    return METHOD;
                case "f":
                    return FIELD;
                case "p":
                    return PARAMETER;
                case "v":
                    return LOCAL_VARIABLE;
                default:
                    throw new IllegalArgumentException("Invalid identifier \"" + identifier + "\"!");
            }
        }
        
        boolean checkPartCount(int indent, int partCount, int namespaceCount) {
            return partCount - indent == (namespaced ? namespaceCount + actualParts : actualParts);
        }
        
        abstract boolean checkStack(TinyState[] stack, int currentIndent);
        
        abstract void visit(Visitor visitor, String[] parts, int indent, boolean escapedStrings);
        
        PartGetter makeGetter(String[] parts, int indent, boolean escapedStrings) {
            return new PartGetter(indent + actualParts, parts, escapedStrings);
        }
    }
    
    private static final String TO_ESCAPE = "\\\n\r\0\t";
    private static final String ESCAPED = "\\nr0t";
    
    private static String unescapeOpt(String raw, boolean escapedStrings) {
        return escapedStrings ? unescape(raw) : raw;
    }
    
    private static String unescape(String str) {
        // copied from matcher, lazy!
        int pos = str.indexOf('\\');
        if (pos < 0) return str;
        
        StringBuilder ret = new StringBuilder(str.length() - 1);
        int start = 0;
        
        do {
            ret.append(str, start, pos);
            pos++;
            int type;
            
            if (pos >= str.length()) {
                throw new RuntimeException("incomplete escape sequence at the end");
            } else if ((type = ESCAPED.indexOf(str.charAt(pos))) < 0) {
                throw new RuntimeException("invalid escape character: \\" + str.charAt(pos));
            } else {
                ret.append(TO_ESCAPE.charAt(type));
            }
            
            start = pos + 1;
        } while ((pos = str.indexOf('\\', start)) >= 0);
        
        ret.append(str, start, str.length());
        
        return ret.toString();
    }
    
    private static final class PartGetter {
        private final int offset;
        private final String[] parts;
        private final boolean escapedStrings;
        
        PartGetter(int offset, String[] parts, boolean escapedStrings) {
            this.offset = offset;
            this.parts = parts;
            this.escapedStrings = escapedStrings;
        }
        
        public String get(int namespace) {
            int index = offset + namespace;
            while (parts[index].isEmpty())
                index--;
            return unescapeOpt(parts[index], escapedStrings);
        }
        
        public String getRaw(int namespace) {
            return unescapeOpt(parts[offset + namespace], escapedStrings);
        }
        
        public String[] getRawNames() {
            if (!escapedStrings) {
                return Arrays.copyOfRange(parts, offset, parts.length);
            }
            
            final String[] ret = new String[parts.length - offset];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = unescape(parts[i + offset]);
            }
            return ret;
        }
        
        public String[] getAllNames() {
            final String[] ret = getRawNames();
            for (int i = 1; i < ret.length; i++) {
                if (ret[i].isEmpty()) {
                    ret[i] = ret[i - 1];
                }
            }
            return ret;
        }
    }
}
