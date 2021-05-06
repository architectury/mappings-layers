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

package dev.architectury.mappingslayers.impl.serializer;

import dev.architectury.mappingslayers.impl.tiny.MappedImpl;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.*;

import java.util.Map;

public class TinyTreeSerializer {
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
        builder.append('c');
        writeMapped(1, metadata, def, builder);
        for (MethodDef methodDef : def.getMethods()) {
            writeMethod(metadata, methodDef, builder);
        }
        for (FieldDef fieldDef : def.getFields()) {
            writeField(metadata, fieldDef, builder);
        }
    }
    
    private static void writeMethod(TinyMetadata metadata, MethodDef def, StringBuilder builder) {
        builder.append('\t').append('m');
        builder.append('\t').append(def.getDescriptor(metadata.getNamespaces().get(0)));
        writeMapped(2, metadata, def, builder);
        for (ParameterDef parameter : def.getParameters()) {
            builder.append("\t\tp\t").append(parameter.getLocalVariableIndex());
            writeMapped(-1, metadata, parameter, builder);
        }
    }
    
    private static void writeField(TinyMetadata metadata, FieldDef def, StringBuilder builder) {
        builder.append('\t').append('f');
        builder.append('\t').append(def.getDescriptor(metadata.getNamespaces().get(0)));
        writeMapped(2, metadata, def, builder);
    }
    
    private static void writeMapped(int indent, TinyMetadata metadata, Mapped mapped, StringBuilder builder) {
        String[] names = MappedImpl.buildNames(metadata, mapped);
        for (String name : names) {
            builder.append('\t');
            builder.append(name);
        }
        builder.append('\n');
        String comment = mapped.getComment();
        if (comment != null && indent >= 0) {
            for (int i = 0; i < indent; i++) {
                builder.append('\t');
            }
            builder.append('c').append('\t').append(escapeString(comment)).append('\n');
        }
    }
    
    private static void writeHeader(TinyMetadata metadata, StringBuilder builder) {
        builder.append("tiny\t");
        builder.append(metadata.getMajorVersion());
        builder.append('\t');
        builder.append(metadata.getMinorVersion());
        
        for (String namespace : metadata.getNamespaces()) {
            builder.append('\t');
            builder.append(namespace);
        }
        
        builder.append('\n');
        for (Map.Entry<String, String> entry : metadata.getProperties().entrySet()) {
            builder.append('\t');
            builder.append(entry.getKey());
            if (entry.getValue() != null) {
                builder.append('\t');
                builder.append(escapeString(entry.getValue()));
            }
            builder.append('\n');
        }
    }
    
    private static StringBuilder escapeString(String string) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '\\') builder.append("\\\\");
            else if (c == '\n') builder.append("\\n");
            else if (c == '\r') builder.append("\\r");
            else if (c == '\t') builder.append("\\t");
            else if (c == '\0') builder.append("\\0");
            else builder.append(c);
        }
        return builder;
    }
}
