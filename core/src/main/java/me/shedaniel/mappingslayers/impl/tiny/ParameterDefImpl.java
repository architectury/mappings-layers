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

import me.shedaniel.mappingslayers.api.MappingsEntryType;
import me.shedaniel.mappingslayers.api.mutable.MutableParameterDef;
import org.jetbrains.annotations.Nullable;

public class ParameterDefImpl extends MappedImpl implements MutableParameterDef {
    private int lvIndex;
    
    public ParameterDefImpl(TinyTreeImpl namespaceGetter, String[] names, @Nullable String comment, int lvIndex) {
        super(namespaceGetter, names, comment);
        this.lvIndex = lvIndex;
    }
    
    @Override
    public void setLocalVariableIndex(int index) {
        this.lvIndex = index;
    }
    
    @Override
    public int getLocalVariableIndex() {
        return this.lvIndex;
    }
    
    @Override
    public MappingsEntryType getType() {
        return MappingsEntryType.PARAMETER;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterDefImpl)) return false;
        if (!super.equals(o)) return false;
        
        ParameterDefImpl that = (ParameterDefImpl) o;
    
        return lvIndex == that.lvIndex;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + lvIndex;
        return result;
    }
}
