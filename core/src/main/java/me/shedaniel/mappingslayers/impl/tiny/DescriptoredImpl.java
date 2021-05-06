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

import me.shedaniel.mappingslayers.api.mutable.MutableDescriptored;
import me.shedaniel.mappingslayers.api.utils.MappingsUtils;
import org.jetbrains.annotations.Nullable;

public abstract class DescriptoredImpl extends MappedImpl implements MutableDescriptored {
    private String descriptor;
    
    public DescriptoredImpl(TinyTreeImpl parent, String[] names, @Nullable String comment, String descriptor) {
        super(parent, names, comment);
        this.descriptor = descriptor;
    }
    
    @Override
    public void setPrimaryDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
    
    @Override
    public void setDescriptor(String namespace, String descriptor) {
        setDescriptor(this.parent.applyAsInt(namespace), descriptor);
    }
    
    @Override
    public void setDescriptor(int namespace, String descriptor) {
        if (namespace == 0) {
            setPrimaryDescriptor(descriptor);
        } else {
            setPrimaryDescriptor(MappingsUtils.remapDescriptorToPrimary(parent, descriptor, namespace));
        }
    }
    
    @Override
    public String getDescriptor(String namespace) {
        return getDescriptor(this.parent.applyAsInt(namespace));
    }
    
    @Override
    public String getDescriptor(int namespace) {
        if (namespace == 0) {
            return descriptor;
        } else {
            return MappingsUtils.remapDescriptorFromPrimary(parent, descriptor, namespace);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DescriptoredImpl)) return false;
        if (!super.equals(o)) return false;
        
        DescriptoredImpl that = (DescriptoredImpl) o;
    
        return descriptor.equals(that.descriptor);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + descriptor.hashCode();
        return result;
    }
}
