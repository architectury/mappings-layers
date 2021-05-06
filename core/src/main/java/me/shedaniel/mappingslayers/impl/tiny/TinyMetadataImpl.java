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

import me.shedaniel.mappingslayers.api.mutable.MutableTinyMetadata;
import net.fabricmc.mapping.reader.v2.TinyMetadata;

import java.util.*;

public class TinyMetadataImpl implements TinyMetadata, MutableTinyMetadata {
    private int majorVersion;
    private int minorVersion;
    private final List<String> namespaces;
    private final Map<String, String> properties;
    private final Map<String, Integer> index;
    
    public TinyMetadataImpl(TinyMetadata metadata) {
        this(metadata.getMajorVersion(), metadata.getMinorVersion(), metadata.getNamespaces(), metadata.getProperties());
    }
    
    public TinyMetadataImpl(int majorVersion, int minorVersion, List<String> namespaces, Map<String, String> properties) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.namespaces = Collections.unmodifiableList(new ArrayList<>(namespaces));
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
    
    @Override
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }
    
    @Override
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }
    
    @Override
    public MutableTinyMetadata withNewNamespaces(List<String> newNamespaces) {
        return new TinyMetadataImpl(getMajorVersion(), getMinorVersion(), newNamespaces, getProperties());
    }
    
    @Override
    public MutableTinyMetadata copy() {
        return withNewNamespaces(namespaces);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TinyMetadataImpl)) return false;
        
        TinyMetadataImpl that = (TinyMetadataImpl) o;
        
        if (majorVersion != that.majorVersion) return false;
        if (minorVersion != that.minorVersion) return false;
        if (!namespaces.equals(that.namespaces)) return false;
        if (!properties.equals(that.properties)) return false;
        return index.equals(that.index);
    }
    
    @Override
    public int hashCode() {
        int result = majorVersion;
        result = 31 * result + minorVersion;
        result = 31 * result + namespaces.hashCode();
        result = 31 * result + properties.hashCode();
        result = 31 * result + index.hashCode();
        return result;
    }
}
