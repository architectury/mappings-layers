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

import net.fabricmc.mapping.tree.Mapped;
import org.jetbrains.annotations.Nullable;

public interface MutableMapped extends Mapped, MappingsEntry {
    void setName(int namespace, String name);
    
    void setName(String namespace, String name);
    
    void setComment(@Nullable String comment);
    
    String getName(int namespace);
    
    String getRawName(int namespace);
    
    @Override
    default void setMapped(String mapped) {
        setName(NS_NAMED, mapped);
    }
    
    @Override
    default String getMapped() {
        return getRawName(NS_NAMED);
    }
    
    @Override
    default String getIntermediary() {
        return getName(NS_INTERMEDIARY);
    }
}
