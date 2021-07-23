/*
 * This file is part of architectury.
 * Copyright (C) 2021 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package dev.architectury.mappingslayers.api.utils;

import dev.architectury.mappingslayers.api.MappingsEntryType;
import dev.architectury.mappingslayers.api.mutable.*;
import dev.architectury.mappingslayers.impl.mappings.TinyReader;
import dev.architectury.mappingslayers.impl.mappings.Tsrg2Reader;
import dev.architectury.mappingslayers.impl.mappings.Tsrg2Writer;
import dev.architectury.mappingslayers.impl.serializer.TinyTreeSerializer;
import dev.architectury.mappingslayers.impl.tiny.TinyTreeImpl;
import dev.architectury.mappingslayers.impl.tiny.utils.TinyTreeEntryIterator;
import dev.architectury.refmapremapper.utils.DescriptorRemapper;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static dev.architectury.mappingslayers.impl.mappings.MappingsMerger.copyOverNames;
import static dev.architectury.mappingslayers.impl.mappings.MappingsMerger.copyOverNamesDescriptored;

public final class MappingsUtils {
    /**
     * Remaps a descriptor from {@code from} to {@code to} namespace.
     *
     * @param tree       the mappings tree
     * @param descriptor the descriptor in the {@code from} namespace
     * @param from       the original namespace index
     * @param to         the destination namespace index
     * @return the descriptor in the {@code to} namespace
     */
    public static String remapDescriptor(TinyTree tree, String descriptor, int from, int to) {
        if (from == 0) {
            return remapDescriptorFromPrimary(tree, descriptor, to);
        }
        return remapDescriptor(tree, descriptor, namespace(tree, from), namespace(tree, to));
    }
    
    /**
     * Remaps a descriptor from {@code from} to {@code to} namespace.
     *
     * @param tree       the mappings tree
     * @param descriptor the descriptor in the {@code from} namespace
     * @param from       the original namespace
     * @param to         the destination namespace
     * @return the descriptor in the {@code to} namespace
     */
    public static String remapDescriptor(TinyTree tree, String descriptor, String from, String to) {
        String primaryNamespace = tree.getMetadata().getNamespaces().get(0);
        if (from.equals(primaryNamespace)) {
            return remapDescriptorFromPrimary(tree, descriptor, to);
        }
        return DescriptorRemapper.remapDescriptor(descriptor, s -> {
            for (ClassDef def : tree.getClasses()) {
                if (def.getRawName(from).equals(s)) {
                    return def.getName(to);
                }
            }
            return s;
        });
    }
    
    /**
     * Remaps a descriptor from {@code from} to the primary namespace.
     *
     * @param tree       the mappings tree
     * @param descriptor the descriptor in the {@code from} namespace
     * @param from       the original namespace index
     * @return the descriptor in the primary namespace
     */
    public static String remapDescriptorToPrimary(TinyTree tree, String descriptor, int from) {
        return remapDescriptorToPrimary(tree, descriptor, namespace(tree, from));
    }
    
    /**
     * Remaps a descriptor from {@code from} to the primary namespace.
     *
     * @param tree       the mappings tree
     * @param descriptor the descriptor in the {@code from} namespace
     * @param from       the original namespace
     * @return the descriptor in the primary namespace
     */
    public static String remapDescriptorToPrimary(TinyTree tree, String descriptor, String from) {
        return remapDescriptor(tree, descriptor, from, tree.getMetadata().getNamespaces().get(0));
    }
    
    /**
     * Remaps a descriptor from the primary to {@code to} namespace.
     *
     * @param tree       the mappings tree
     * @param descriptor the descriptor in the primary namespace
     * @param to         the destination namespace index
     * @return the descriptor in the {@code to} namespace
     */
    public static String remapDescriptorFromPrimary(TinyTree tree, String descriptor, int to) {
        return remapDescriptorFromPrimary(tree, descriptor, namespace(tree, to));
    }
    
    /**
     * Remaps a descriptor from the primary to {@code to} namespace.
     *
     * @param tree       the mappings tree
     * @param descriptor the descriptor in the primary namespace
     * @param to         the destination namespace
     * @return the descriptor in the {@code to} namespace
     */
    public static String remapDescriptorFromPrimary(TinyTree tree, String descriptor, String to) {
        Map<String, ClassDef> classMap = tree.getDefaultNamespaceClassMap();
        return DescriptorRemapper.remapDescriptor(descriptor, s -> {
            ClassDef def = classMap.get(s);
            if (def == null) return s;
            return def.getName(to);
        });
    }
    
    /**
     * Retrieves the namespace from a namespace index.
     *
     * @param tree  the mappings tree
     * @param index the index of the namespace
     * @return the namespace
     */
    public static String namespace(TinyTree tree, int index) {
        return tree.getMetadata().getNamespaces().get(index);
    }
    
    /**
     * Retrieves the namespace index from a namespace.
     *
     * @param tree      the mappings tree
     * @param namespace the namespace
     * @return the index of the namespace
     */
    public static int namespaceId(TinyTree tree, String namespace) {
        return tree.getMetadata().index(namespace);
    }
    
    /**
     * Creates a {@link MutableTinyTree} from a {@link TinyMetadata}.
     *
     * @param metadata the metadata of the mappings tree
     * @return the newly created mappings tree
     */
    public static MutableTinyTree create(TinyMetadata metadata) {
        return new TinyTreeImpl(metadata, Stream.empty());
    }
    
    /**
     * Creates a copy of {@link MutableTinyTree} from a {@link TinyTree}.
     *
     * @param tree the mappings tree to be copied
     * @return the copied mappings tree
     */
    public static MutableTinyTree copyAsMutable(TinyTree tree) {
        return new TinyTreeImpl(tree.getMetadata(), tree.getClasses().stream());
    }
    
    /**
     * Creates a copy of {@link MutableTinyTree} from a {@link TinyTree}, if {@code tree} is not an instance of {@link MutableTinyTree}.
     *
     * @param tree the mappings tree to be copied
     * @return the mutable mappings tree
     */
    public static MutableTinyTree getAsMutable(TinyTree tree) {
        if (tree instanceof MutableTinyTree) return (MutableTinyTree) tree;
        return copyAsMutable(tree);
    }
    
    /**
     * Deserializes a {@link MutableTinyTree} from the tiny format.
     *
     * @param content the content of the mappings in tiny format
     * @return the mappings tree
     */
    public static MutableTinyTree deserializeFromString(String content) {
        try {
            return copyAsMutable(TinyReader.loadWithDetection(new BufferedReader(new StringReader(content))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    /**
     * Deserializes a {@link MutableTinyTree} from the tsrg2 format.
     *
     * @param content the content of the mappings in tsrg2 format
     * @return the mappings tree
     */
    public static MutableTinyTree deserializeFromTsrg2(String content) {
        return Tsrg2Reader.read(IOUtils.lineIterator(new StringReader(content)));
    }
    
    /**
     * Serializes a {@link MutableTinyTree} to the tiny format.
     *
     * @param tree the mappings tree
     * @return the content of the mappings in tiny format
     */
    public static String serializeToString(TinyTree tree) {
        return TinyTreeSerializer.serialize(tree);
    }
    
    /**
     * Serializes a {@link MutableTinyTree} to the tsrg2 format.
     *
     * @param tree the mappings tree
     * @return the content of the mappings in tiny format
     */
    public static String serializeToTsrg2(TinyTree tree) {
        return Tsrg2Writer.serialize(tree);
    }
    
    /**
     * Creates a copy of a mappings tree with namespaces removed.
     *
     * @param tree    the original mappings tree
     * @param removed the namespaces to be removed
     * @return a copy of the mappings tree with namespaces removed
     */
    public static MutableTinyTree removeNamespaces(MutableTinyTree tree, String... removed) {
        return removeNamespaces(tree, Arrays.asList(removed));
    }
    
    /**
     * Creates a copy of a mappings tree with namespaces removed.
     *
     * @param tree    the original mappings tree
     * @param removed the namespaces to be removed
     * @return a copy of the mappings tree with namespaces removed
     */
    public static MutableTinyTree removeNamespaces(MutableTinyTree tree, Collection<String> removed) {
        if (!tree.getMetadata().getNamespaces().containsAll(removed)) {
            throw new IllegalArgumentException("Cannot remove " + String.join(", ", removed) + " from tree with namespaces " + String.join(", ", tree.getMetadata().getNamespaces()));
        }
        
        List<String> newNamespaces = new ArrayList<>(tree.getMetadata().getNamespaces());
        newNamespaces.removeAll(removed);
        return reorderNamespaces(tree, newNamespaces);
    }
    
    /**
     * Creates a copy of a mappings tree with namespaces reordered.
     *
     * @param tree          the original mappings tree
     * @param newNamespaces the new order of the namespaces
     * @return a copy of the mappings tree with namespaces reordered
     */
    public static MutableTinyTree reorderNamespaces(MutableTinyTree tree, List<String> newNamespaces) {
        if (newNamespaces.equals(tree.getMetadata().getNamespaces())) return tree.copy();
        checkCouldReorder(tree, newNamespaces);
        int[] reverseNamespacesMap = new int[newNamespaces.size()];
        for (int i = 0; i < reverseNamespacesMap.length; i++) {
            reverseNamespacesMap[i] = tree.getMetadata().getNamespaces().indexOf(newNamespaces.get(i));
        }
        int primaryInPreviousIndices = reverseNamespacesMap[0];
        MutableTinyTree newTree = create(tree.getMetadata().withNewNamespaces(newNamespaces));
        for (MutableClassDef classDef : tree.getClassesMutable()) {
            MutableClassDef newClassDef = newTree.getOrCreateClass(classDef.getName(primaryInPreviousIndices));
            copyOverNames(classDef, newClassDef, reverseNamespacesMap);
            for (MutableFieldDef fieldDef : classDef.getFieldsMutable()) {
                MutableFieldDef newFieldDef = newClassDef.getOrCreateField(fieldDef.getName(primaryInPreviousIndices), fieldDef.getDescriptor(primaryInPreviousIndices));
                copyOverNamesDescriptored(fieldDef, newFieldDef, reverseNamespacesMap);
            }
            for (MutableMethodDef methodDef : classDef.getMethodsMutable()) {
                MutableMethodDef newMethodDef = newClassDef.getOrCreateMethod(methodDef.getName(primaryInPreviousIndices), methodDef.getDescriptor(primaryInPreviousIndices));
                copyOverNamesDescriptored(methodDef, newMethodDef, reverseNamespacesMap);
                for (MutableParameterDef parameterDef : methodDef.getParametersMutable()) {
                    MutableParameterDef newParameterDef = newMethodDef.getOrCreateParameter(parameterDef.getLocalVariableIndex(), parameterDef.getName(primaryInPreviousIndices));
                    copyOverNames(parameterDef, newParameterDef, reverseNamespacesMap);
                }
            }
        }
        return newTree;
    }
    
    private static void checkCouldReorder(MutableTinyTree tree, List<String> newNamespaces) {
        ArrayList<String> list = new ArrayList<>(tree.getMetadata().getNamespaces());
        String error = "Could not reorder tiny tree from " + String.join(", ", list) + " to " + String.join(", ", newNamespaces);
        if (!list.containsAll(newNamespaces)) {
            throw new IllegalArgumentException(error);
        }
        for (String namespace : newNamespaces) {
            if (!list.contains(namespace)) {
                throw new IllegalArgumentException(error);
            }
        }
    }
    
    public static void sort(MutableTinyTree tree, String namespace) {
        sort(tree, namespaceId(tree, namespace));
    }
    
    public static void sort(MutableTinyTree tree, int namespace) {
        sort(tree, Comparator.<MutableMapped, String>comparing(mapped -> mapped.getName(namespace))
                .thenComparing((o1, o2) -> {
                    if (o1 instanceof MutableDescriptored && o2 instanceof MutableDescriptored) {
                        return ((MutableDescriptored) o1).getDescriptor(namespace)
                                .compareTo(((MutableDescriptored) o2).getDescriptor(namespace));
                    }
                    return 0;
                }));
    }
    
    public static void sort(MutableTinyTree tree, Comparator<MutableMapped> comparator) {
        tree.getClassesMutable().sort(comparator);
        for (MutableClassDef classDef : tree.getClassesMutable()) {
            classDef.getMethodsMutable().sort(comparator);
            classDef.getFieldsMutable().sort(comparator);
            for (MutableMethodDef methodDef : classDef.getMethodsMutable()) {
                methodDef.getParametersMutable().sort(comparator);
            }
        }
    }
    
    public static Predicate<MappingsEntryType> filterTypes(MappingsEntryType... types) {
        return fromTypeId(getTypeId(types));
    }
    
    public static int getTypeId(MappingsEntryType... types) {
        Set<MappingsEntryType> list = new HashSet<>(Arrays.asList(types));
        int i = 0;
        i |= (list.contains(MappingsEntryType.CLASS) ? 1 : 0);
        i |= (list.contains(MappingsEntryType.METHOD) ? 1 : 0) << 1;
        i |= (list.contains(MappingsEntryType.FIELD) ? 1 : 0) << 2;
        i |= (list.contains(MappingsEntryType.PARAMETER) ? 1 : 0) << 3;
        i |= (list.contains(MappingsEntryType.LOCAL_VARIABLE) ? 1 : 0) << 4;
        return i;
    }
    
    public static int getTypeId(Predicate<MappingsEntryType> predicate) {
        int i = 0;
        i |= (predicate.test(MappingsEntryType.CLASS) ? 1 : 0);
        i |= (predicate.test(MappingsEntryType.METHOD) ? 1 : 0) << 1;
        i |= (predicate.test(MappingsEntryType.FIELD) ? 1 : 0) << 2;
        i |= (predicate.test(MappingsEntryType.PARAMETER) ? 1 : 0) << 3;
        i |= (predicate.test(MappingsEntryType.LOCAL_VARIABLE) ? 1 : 0) << 4;
        return i;
    }
    
    public static Predicate<MappingsEntryType> fromTypeId(int id) {
        return type -> {
            switch (type) {
                case CLASS:
                    return (id & 1) == 1;
                case METHOD:
                    return (id >> 1 & 1) == 1;
                case FIELD:
                    return (id >> 2 & 1) == 1;
                case PARAMETER:
                    return (id >> 3 & 1) == 1;
                case LOCAL_VARIABLE:
                    return (id >> 4 & 1) == 1;
            }
            return false;
        };
    }
    
    public static void walk(MutableTinyTree tree, Consumer<MutableMapped> consumer) {
        walk(tree, type -> true, consumer);
    }
    
    public static void walk(MutableTinyTree tree, Predicate<MappingsEntryType> typePredicate, Consumer<MutableMapped> consumer) {
        TinyTreeEntryIterator.iterate(tree, typePredicate, consumer);
    }
}
