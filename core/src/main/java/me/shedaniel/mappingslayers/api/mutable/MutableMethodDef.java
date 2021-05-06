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

import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.tree.ParameterDef;

import java.util.Collection;
import java.util.List;

public interface MutableMethodDef extends MethodDef, MutableDescriptored {
    List<MutableParameterDef> getParametersMutable();
    
    @Override
    default Collection<ParameterDef> getParameters() {
        return (Collection<ParameterDef>) (Collection<? extends ParameterDef>) getParametersMutable();
    }
    
    MutableParameterDef getOrCreateParameter(int lvIndex, String primaryName);
}
