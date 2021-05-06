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

package me.shedaniel.mappingslayers.api.transform;

import me.shedaniel.mappingslayers.api.MappingsEntryType;
import me.shedaniel.mappingslayers.api.mutable.MutableMapped;
import me.shedaniel.mappingslayers.api.mutable.MutableTinyTree;
import me.shedaniel.mappingslayers.api.utils.MappingsUtils;

public interface SimpleMappingsTransformation extends MappingsTransformation {
    void handle(MutableMapped entry);
    
    boolean handleType(MappingsEntryType type);
    
    @Override
    default void handle(MutableTinyTree tree) {
        MappingsUtils.walk(tree, this::handleType, this::handle);
    }
}
