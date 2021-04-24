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

import me.shedaniel.mappingslayers.api.MappingsEntryType;

public interface MappingsEntry {
    String NS_NAMED = "named";
    String NS_INTERMEDIARY = "intermediary";
    
    default boolean isClass() {
        return getType() == MappingsEntryType.CLASS;
    }
    
    default boolean isMethod() {
        return getType() == MappingsEntryType.METHOD;
    }
    
    default boolean isField() {
        return getType() == MappingsEntryType.FIELD;
    }
    
    default boolean isParameter() {
        return getType() == MappingsEntryType.PARAMETER;
    }
    
    MappingsEntryType getType();
    
    void setMapped(String mapped);
    
    String getMapped();
    
    default String getMappedNullable() {
        String mapped = getMapped();
        if (mapped.isEmpty()) return null;
        return mapped;
    }
    
    String getIntermediary();
    
    default void unmap() {
        setMapped("");
    }
    
    default boolean isUnmapped() {
        return getMapped().isEmpty();
    }
    
    default boolean isMapped() {
        return getMapped().length() > 0;
    }
}
