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
import me.shedaniel.mappingslayers.api.mutable.MutableMethodDef;
import me.shedaniel.mappingslayers.api.mutable.MutableParameterDef;
import net.fabricmc.mapping.tree.LocalVariableDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.tree.ParameterDef;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MethodDefImpl extends DescriptoredImpl implements MutableMethodDef {
    private final List<MutableParameterDef> parameters;
    private final Collection<LocalVariableDef> localVariables;
    
    public MethodDefImpl(TinyTreeImpl parent, String[] names, @Nullable String comment, String descriptor,
            List<MutableParameterDef> parameters, Collection<LocalVariableDef> localVariables) {
        super(parent, names, comment, descriptor);
        this.parameters = parameters;
        this.localVariables = localVariables;
    }
    
    public MethodDefImpl(TinyTreeImpl parent, String[] names, @Nullable String comment, String descriptor,
            Collection<ParameterDef> parameters, Collection<LocalVariableDef> localVariables) {
        this(parent, names, comment, descriptor, (List<MutableParameterDef>) parameters.stream().<MutableParameterDef>map(def ->
                new ParameterDefImpl(parent, buildNames(parent.getMetadata(), def), def.getComment(), def.getLocalVariableIndex()))
                .collect(Collectors.toList()), localVariables);
    }
    
    public MethodDefImpl(TinyTreeImpl parent, MethodDef def) {
        this(parent, buildNames(parent.getMetadata(), def), def.getComment(),
                def.getDescriptor(parent.getMetadata().getNamespaces().get(0)),
                def.getParameters(), def.getLocalVariables());
    }
    
    @Override
    public List<MutableParameterDef> getParametersMutable() {
        return parameters;
    }
    
    @Override
    public Collection<LocalVariableDef> getLocalVariables() {
        return localVariables;
    }
    
    @Override
    public MappingsEntryType getType() {
        return MappingsEntryType.METHOD;
    }
}
