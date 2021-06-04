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

package dev.architectury.mappingslayers.api.mutable;

import dev.architectury.mappingslayers.api.utils.MappingsUtils;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MutableTinyTree extends TinyTree {
    MutableTinyMetadata getMetadata();
    
    List<MutableClassDef> getClassesMutable();
    
    Map<String, MutableClassDef> getDefaultNamespaceClassMapMutable();
    
    @Override
    default Collection<ClassDef> getClasses() {
        return (Collection<ClassDef>) (Collection<? extends ClassDef>) getClassesMutable();
    }
    
    @Override
    default Map<String, ClassDef> getDefaultNamespaceClassMap() {
        return (Map<String, ClassDef>) (Map<String, ? extends ClassDef>) getDefaultNamespaceClassMapMutable();
    }
    
    default MutableTinyTree copy() {
        return MappingsUtils.copyAsMutable(this);
    }
    
    MutableClassDef getOrCreateClass(String primaryName);
    
    MutableClassDef constructClass(String primaryName);
}
