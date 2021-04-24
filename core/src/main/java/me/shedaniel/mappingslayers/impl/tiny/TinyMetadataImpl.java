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

import net.fabricmc.mapping.reader.v2.TinyMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TinyMetadataImpl implements TinyMetadata {
    private final int majorVersion;
    private final int minorVersion;
    private final List<String> namespaces;
    private final Map<String, String> properties;
    private final Map<String, Integer> index;
    
    public TinyMetadataImpl(int majorVersion, int minorVersion, List<String> namespaces, Map<String, String> properties) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.namespaces = new ArrayList<>(namespaces);
        this.properties = new HashMap<>(properties);
        this.index = new HashMap<>();
        for (int i = 0; i < namespaces.size(); i++) {
            index.put(namespaces.get(i), i);
        }
    }
    
    @Override
    public int index(String namespace) {
        return index.get(namespace);
    }
    
    @Override
    public int getMajorVersion() {
        return majorVersion;
    }
    
    @Override
    public int getMinorVersion() {
        return minorVersion;
    }
    
    @Override
    public List<String> getNamespaces() {
        return namespaces;
    }
    
    @Override
    public Map<String, String> getProperties() {
        return properties;
    }
}
