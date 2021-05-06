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

package me.shedaniel.mappingslayers.api.mutable;

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.MethodDef;

import java.util.List;

public interface MutableClassDef extends ClassDef, MutableMapped {
    List<MutableMethodDef> getMethodsMutable();
    
    List<MutableFieldDef> getFieldsMutable();
    
    @Override
    default List<MethodDef> getMethods() {
        return (List<MethodDef>) (List<? extends MethodDef>) getMethodsMutable();
    }
    
    @Override
    default List<FieldDef> getFields() {
        return (List<FieldDef>) (List<? extends FieldDef>) getFieldsMutable();
    }
    
    MutableFieldDef getOrCreateField(String primaryName, String primaryDescriptor);
    
    MutableMethodDef getOrCreateMethod(String primaryName, String primaryDescriptor);
}
