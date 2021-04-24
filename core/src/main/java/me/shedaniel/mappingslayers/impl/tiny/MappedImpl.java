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

import me.shedaniel.mappingslayers.api.mutable.MutableMapped;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.Mapped;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public abstract class MappedImpl implements MutableMapped {
    protected final ToIntFunction<String> namespaceGetter;
    private final String[] names;
    @Nullable
    private String comment;
    
    public MappedImpl(ToIntFunction<String> namespaceGetter, String[] names, @Nullable String comment) {
        this.namespaceGetter = namespaceGetter;
        this.names = names;
        this.comment = comment;
    }
    
    @Override
    public String getName(String namespace) {
        return getName(namespaceGetter.applyAsInt(namespace));
    }
    
    @Override
    public String getRawName(String namespace) {
        return names[namespaceGetter.applyAsInt(namespace)];
    }
    
    @Override
    public String getName(int namespace) {
        if (namespace >= names.length)
            namespace = names.length - 1;
        while (names[namespace].isEmpty()) {
            if (namespace == 0)
                return "";
            namespace--;
        }
        return names[namespace];
    }
    
    @Override
    public String getRawName(int namespace) {
        return names[namespace];
    }
    
    @Override
    public void setName(String namespace, String name) {
        names[namespaceGetter.applyAsInt(namespace)] = name;
    }
    
    @Nullable
    @Override
    public String getComment() {
        return comment;
    }
    
    @Override
    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }
    
    public static String[] buildNames(TinyMetadata metadata, Mapped mapped) {
        if (mapped instanceof MappedImpl) return Arrays.copyOf(((MappedImpl) mapped).names, ((MappedImpl) mapped).names.length);
        String[] names = new String[metadata.getNamespaces().size()];
        for (int i = 0; i < metadata.getNamespaces().size(); i++) {
            names[i] = mapped.getRawName(metadata.getNamespaces().get(i));
        }
        return names;
    }
}
