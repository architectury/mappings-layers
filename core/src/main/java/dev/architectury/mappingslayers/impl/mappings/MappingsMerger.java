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

package dev.architectury.mappingslayers.impl.mappings;

import dev.architectury.mappingslayers.api.mutable.MutableDescriptored;
import dev.architectury.mappingslayers.api.mutable.MutableMapped;

public class MappingsMerger {
    public static void copyOverNamesDescriptored(MutableDescriptored def, MutableDescriptored newDef, int[] mapper) {
        copyOverNames(def, newDef, mapper);
        newDef.setPrimaryDescriptor(def.getDescriptor(mapper[0]));
    }
    
    public static void copyOverNames(MutableMapped def, MutableMapped newDef, int[] mapper) {
        for (int i = 1; i < mapper.length; i++) {
            newDef.setName(i, def.getName(mapper[i]));
        }
        newDef.setComment(def.getComment());
    }
}
