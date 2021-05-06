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

package me.shedaniel.mappingslayers.impl.mappings;

import me.shedaniel.mappingslayers.api.Mappings;
import me.shedaniel.mappingslayers.api.MappingsReaders;
import me.shedaniel.mappingslayers.api.mutable.MutableTinyTree;
import me.shedaniel.mappingslayers.api.utils.MappingsUtils;
import me.shedaniel.mappingslayers.impl.serializer.TinyTreeSerializer;
import me.shedaniel.mappingslayers.api.transform.MappingsTransformation;
import net.fabricmc.mapping.tree.TinyTree;

import java.util.List;

public class TinyTreeMappings implements Mappings {
    private MutableTinyTree tree;
    private String uuid;
    
    public TinyTreeMappings(TinyTree tree, String uuid) {
        this.tree = MappingsUtils.copyAsMutable(tree);
        this.uuid = uuid;
    }
    
    @Override
    public String serializeToTiny() {
        return TinyTreeSerializer.serialize(tree);
    }
    
    @Override
    public String uuid() {
        return uuid;
    }
    
    @Override
    public Mappings withTransformations(List<MappingsTransformation> transformations) {
        for (MappingsTransformation transformation : transformations) {
            transformation.handle(tree);
        }
        return this;
    }
}
