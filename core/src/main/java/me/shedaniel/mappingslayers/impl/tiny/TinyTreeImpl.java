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

package me.shedaniel.mappingslayers.impl.tiny;

import com.google.common.collect.ForwardingList;
import me.shedaniel.mappingslayers.api.mutable.MutableClassDef;
import me.shedaniel.mappingslayers.api.mutable.MutableTinyMetadata;
import me.shedaniel.mappingslayers.api.mutable.MutableTinyTree;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.ClassDef;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TinyTreeImpl implements MutableTinyTree, ToIntFunction<String> {
    private final MutableTinyMetadata metadata;
    private final Map<String, MutableClassDef> classMap = new HashMap<>();
    private final ClassList classes;
    private final String primaryNamespace;
    
    public TinyTreeImpl(TinyMetadata metadata, Stream<ClassDef> classes) {
        this.metadata = new TinyMetadataImpl(metadata);
        this.classes = new ClassList(classes.map(def -> ClassDefImpl.of(this, def)).collect(Collectors.toList()));
        this.primaryNamespace = this.metadata.getNamespaces().get(0);
        for (MutableClassDef classDef : this.classes) {
            this.classMap.put(classDef.getName(primaryNamespace), classDef);
        }
    }
    
    @Override
    public MutableTinyMetadata getMetadata() {
        return metadata;
    }
    
    @Override
    public Map<String, MutableClassDef> getDefaultNamespaceClassMapMutable() {
        return classMap;
    }
    
    @Override
    public List<MutableClassDef> getClassesMutable() {
        return classes;
    }
    
    @Override
    public int applyAsInt(String value) {
        return metadata.index(value);
    }
    
    @Override
    public MutableClassDef getOrCreateClass(String primaryName) {
        MutableClassDef def = classMap.get(primaryName);
        if (def != null)
            return def;
        String[] names = new String[getMetadata().getNamespaces().size()];
        names[0] = primaryName;
        for (int i = 1; i < names.length; i++) {
            names[i] = "";
        }
        ClassDefImpl classDef = new ClassDefImpl(this, names, null, new ArrayList<>(), new ArrayList<>());
        classes.add(classDef);
        return classDef;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TinyTreeImpl)) return false;
        
        TinyTreeImpl tinyTree = (TinyTreeImpl) o;
        
        if (!metadata.equals(tinyTree.metadata)) return false;
        return classes.equals(tinyTree.classes);
    }
    
    @Override
    public int hashCode() {
        int result = metadata.hashCode();
        result = 31 * result + classes.hashCode();
        return result;
    }
    
    private class ClassList extends ForwardingList<MutableClassDef> {
        private final List<MutableClassDef> parent;
        
        public ClassList(List<MutableClassDef> parent) {
            this.parent = parent;
        }
        
        @Override
        protected List<MutableClassDef> delegate() {
            return parent;
        }
        
        @Override
        public boolean add(MutableClassDef element) {
            boolean add = super.add(element);
            if (add) classMap.put(element.getName(primaryNamespace), element);
            return add;
        }
        
        @Override
        public void add(int index, MutableClassDef element) {
            super.add(index, element);
            classMap.put(element.getName(primaryNamespace), element);
        }
        
        @Override
        public boolean addAll(Collection<? extends MutableClassDef> elements) {
            boolean add = super.addAll(elements);
            if (add) {
                for (MutableClassDef element : elements) {
                    classMap.put(element.getName(primaryNamespace), element);
                }
            }
            return add;
        }
        
        @Override
        public boolean addAll(int index, Collection<? extends MutableClassDef> elements) {
            boolean add = super.addAll(index, elements);
            if (add) {
                for (MutableClassDef classDef : elements) {
                    classMap.put(classDef.getName(primaryNamespace), classDef);
                }
            }
            return add;
        }
        
        @Override
        public boolean retainAll(Collection<?> elements) {
            boolean retained = super.retainAll(elements);
            if (retained) {
                classMap.clear();
                for (MutableClassDef classDef : (Collection<MutableClassDef>) elements) {
                    classMap.put(classDef.getName(primaryNamespace), classDef);
                }
            }
            return retained;
        }
        
        @Override
        public MutableClassDef remove(int index) {
            MutableClassDef removed = super.remove(index);
            if (removed != null) classMap.remove(removed.getName(primaryNamespace));
            return removed;
        }
        
        @Override
        public boolean removeAll(Collection<?> elements) {
            boolean removed = super.removeAll(elements);
            if (removed) {
                for (MutableClassDef classDef : (Collection<MutableClassDef>) elements) {
                    classMap.remove(classDef.getName(primaryNamespace));
                }
            }
            return removed;
        }
        
        @Override
        public boolean removeIf(Predicate<? super MutableClassDef> filter) {
            boolean removed = super.removeIf(filter);
            if (removed) {
                classMap.values().removeIf(filter);
            }
            return removed;
        }
        
        @Override
        public boolean remove(Object element) {
            boolean removed = super.remove(element);
            if (removed && element instanceof MutableClassDef) classMap.remove(((MutableClassDef) element).getName(primaryNamespace));
            return removed;
        }
    }
}
