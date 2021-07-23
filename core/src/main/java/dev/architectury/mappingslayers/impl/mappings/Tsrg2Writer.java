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

import dev.architectury.mappingslayers.impl.tiny.MappedImpl;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.*;

public class Tsrg2Writer {
    public static String serialize(TinyTree tree) {
        TinyMetadata metadata = tree.getMetadata();
        StringBuilder builder = new StringBuilder();
        writeHeader(metadata, builder);
        
        for (ClassDef classDef : tree.getClasses()) {
            writeClass(metadata, classDef, builder);
        }
        
        return builder.toString();
    }
    
    private static void writeClass(TinyMetadata metadata, ClassDef def, StringBuilder builder) {
        writeMapped(false, metadata, def, builder);
        for (MethodDef methodDef : def.getMethods()) {
            writeMethod(metadata, methodDef, builder);
        }
        for (FieldDef fieldDef : def.getFields()) {
            writeMapped(true, metadata, fieldDef, builder);
        }
    }
    
    private static void writeMethod(TinyMetadata metadata, MethodDef def, StringBuilder builder) {
        writeMapped(true, metadata, def, builder);
        for (ParameterDef parameter : def.getParameters()) {
            builder.append("\t\t").append(parameter.getLocalVariableIndex());
            writeMapped(true, metadata, parameter, builder);
        }
    }
    
    private static void writeField(TinyMetadata metadata, FieldDef def, StringBuilder builder) {
        writeMapped(true, metadata, def, builder);
    }
    
    private static void writeMapped(boolean needFirst, TinyMetadata metadata, Mapped mapped, StringBuilder builder) {
        String[] names = MappedImpl.buildNames(metadata, mapped);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (i == 0) {
                if (needFirst) {
                    builder.append('\t');
                }
            } else {
                builder.append(' ');
            }
            builder.append(name);
            
            if (i == 0 && mapped instanceof Descriptored) {
                String descriptor = ((Descriptored) mapped).getDescriptor(metadata.getNamespaces().get(0));
                
                if (descriptor != null && !descriptor.isEmpty()) {
                    builder.append(' ');
                    builder.append(descriptor);
                }
            }
        }
        builder.append('\n');
    }
    
    private static void writeHeader(TinyMetadata metadata, StringBuilder builder) {
        builder.append("tsrg2");
        
        for (String namespace : metadata.getNamespaces()) {
            builder.append(' ');
            builder.append(namespace);
        }
        
        builder.append('\n');
    }
}
