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

import java.util.Collection;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class ClassDefImpl extends MappedImpl implements MutableClassDef {
    protected final List<MutableMethodDef> methods;
    protected final List<MutableFieldDef> fields;
    
    public ClassDefImpl(ToIntFunction<String> namespaceGetter, String[] names, @Nullable String comment,
            List<MutableMethodDef> methods, List<MutableFieldDef> fields) {
        super(namespaceGetter, names, comment);
        this.methods = methods;
        this.fields = fields;
    }
    
    public ClassDefImpl(TinyTreeImpl parent, String[] names, @Nullable String comment,
            Collection<MethodDef> methods, Collection<FieldDef> fields) {
        this((ToIntFunction<String>) parent, names, comment, methods.stream().map(def -> new MethodDefImpl(parent, def)).collect(Collectors.toList()),
                fields.stream().map(def -> new FieldDefImpl(parent, def)).collect(Collectors.toList()));
    }
    
    public ClassDefImpl(TinyTreeImpl parent, ClassDef def) {
        this(parent, buildNames(parent.getMetadata(), def), def.getComment(),
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
}
