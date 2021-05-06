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

import me.shedaniel.mappingslayers.api.MappingsEntryType;
import me.shedaniel.mappingslayers.api.mutable.MutableClassDef;
import me.shedaniel.mappingslayers.api.mutable.MutableFieldDef;
import me.shedaniel.mappingslayers.api.mutable.MutableMethodDef;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.MethodDef;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ClassDefImpl extends MappedImpl implements MutableClassDef {
    protected final List<MutableMethodDef> methods;
    protected final List<MutableFieldDef> fields;
    
    public ClassDefImpl(TinyTreeImpl namespaceGetter, String[] names, @Nullable String comment,
            List<MutableMethodDef> methods, List<MutableFieldDef> fields) {
        super(namespaceGetter, names, comment);
        this.methods = methods;
        this.fields = fields;
    }
    
    public static ClassDefImpl of(TinyTreeImpl parent, String[] names, @Nullable String comment,
            Collection<MethodDef> methods, Collection<FieldDef> fields) {
        return new ClassDefImpl(parent, names, comment, methods.stream().map(def -> MethodDefImpl.of(parent, def)).collect(Collectors.toList()),
                fields.stream().map(def -> FieldDefImpl.of(parent, def)).collect(Collectors.toList()));
    }
    
    public static ClassDefImpl of(TinyTreeImpl parent, ClassDef def) {
        return of(parent, buildNames(parent.getMetadata(), def), def.getComment(),
                def.getMethods(), def.getFields());
    }
    
    @Override
    public List<MutableMethodDef> getMethodsMutable() {
        return methods;
    }
    
    @Override
    public List<MutableFieldDef> getFieldsMutable() {
        return fields;
    }
    
    @Override
    public MappingsEntryType getType() {
        return MappingsEntryType.CLASS;
    }
    
    @Override
    public MutableFieldDef getOrCreateField(String primaryName, String primaryDescriptor) {
        for (MutableFieldDef fieldDef : getFieldsMutable()) {
            if (fieldDef.getName(0).equals(primaryName) && fieldDef.getDescriptor(0).equals(primaryDescriptor)) {
                return fieldDef;
            }
        }
        String[] names = new String[parent.getMetadata().getNamespaces().size()];
        names[0] = primaryName;
        for (int i = 1; i < names.length; i++) {
            names[i] = "";
        }
        MutableFieldDef def = new FieldDefImpl(parent, names, null, primaryDescriptor);
        fields.add(def);
        return def;
    }
    
    @Override
    public MutableMethodDef getOrCreateMethod(String primaryName, String primaryDescriptor) {
        for (MutableMethodDef methodDef : getMethodsMutable()) {
            if (methodDef.getName(0).equals(primaryName) && methodDef.getDescriptor(0).equals(primaryDescriptor)) {
                return methodDef;
            }
        }
        String[] names = new String[parent.getMetadata().getNamespaces().size()];
        names[0] = primaryName;
        for (int i = 1; i < names.length; i++) {
            names[i] = "";
        }
        MutableMethodDef def = new MethodDefImpl(parent, names, null, primaryDescriptor, new ArrayList<>(), new ArrayList<>());
        methods.add(def);
        return def;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassDefImpl)) return false;
        if (!super.equals(o)) return false;
        
        ClassDefImpl classDef = (ClassDefImpl) o;
        
        if (!methods.equals(classDef.methods)) return false;
        return fields.equals(classDef.fields);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + methods.hashCode();
        result = 31 * result + fields.hashCode();
        return result;
    }
}
