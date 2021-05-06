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

package dev.architectury.mappingslayers.impl.tiny;

import dev.architectury.mappingslayers.api.MappingsEntryType;
import dev.architectury.mappingslayers.api.mutable.MutableFieldDef;
import net.fabricmc.mapping.tree.FieldDef;
import org.jetbrains.annotations.Nullable;

public class FieldDefImpl extends DescriptoredImpl implements MutableFieldDef {
    public FieldDefImpl(TinyTreeImpl parent, String[] names, @Nullable String comment, String descriptor) {
        super(parent, names, comment, descriptor);
    }
    
    public static FieldDefImpl of(TinyTreeImpl parent, FieldDef def) {
        return new FieldDefImpl(parent, buildNames(parent.getMetadata(), def), def.getComment(),
                def.getDescriptor(parent.getMetadata().getNamespaces().get(0)));
    }
    
    @Override
    public MappingsEntryType getType() {
        return MappingsEntryType.FIELD;
    }
}
