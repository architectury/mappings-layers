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

package me.shedaniel.mappingslayers.api.transform;

import me.shedaniel.mappingslayers.api.MappingsEntryType;
import me.shedaniel.mappingslayers.api.mutable.*;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class TinyTreeEntryIterator {
    private TinyTreeEntryIterator() {}
    
    public static int getTypeId(Predicate<MappingsEntryType> typePredicate) {
        int i = 0;
        i |= (typePredicate.test(MappingsEntryType.CLASS) ? 1 : 0);
        i |= (typePredicate.test(MappingsEntryType.METHOD) ? 1 : 0) << 1;
        i |= (typePredicate.test(MappingsEntryType.FIELD) ? 1 : 0) << 2;
        i |= (typePredicate.test(MappingsEntryType.PARAMETER) ? 1 : 0) << 3;
        i |= (typePredicate.test(MappingsEntryType.LOCAL_VARIABLE) ? 1 : 0) << 4;
        return i;
    }
    
    public static Predicate<MappingsEntryType> fromTypeId(int id) {
        return type -> {
            switch (type) {
                case CLASS:
                    return (id & 1) == 1;
                case METHOD:
                    return (id >> 1 & 1) == 1;
                case FIELD:
                    return (id >> 2 & 1) == 1;
                case PARAMETER:
                    return (id >> 3 & 1) == 1;
                case LOCAL_VARIABLE:
                    return (id >> 4 & 1) == 1;
            }
            return false;
        };
    }
    
    public static void iterate(MutableTinyTree tree, Consumer<MappingsEntry> entryConsumer) {
        iterate(tree, type -> true, entryConsumer);
    }
    
    public static void iterate(MutableTinyTree tree, Predicate<MappingsEntryType> typePredicate, Consumer<MappingsEntry> entryConsumer) {
        boolean doClasses = typePredicate.test(MappingsEntryType.CLASS);
        boolean doMethods = typePredicate.test(MappingsEntryType.METHOD);
        boolean doFields = typePredicate.test(MappingsEntryType.FIELD);
        boolean doParameters = typePredicate.test(MappingsEntryType.PARAMETER);
        boolean doMethodsActually = doMethods || doParameters;
        for (MutableClassDef def : tree.getClassesMutable()) {
            if (doClasses) entryConsumer.accept(def);
            if (doMethodsActually) for (MutableMethodDef methodDef : def.getMethodsMutable()) {
                if (doMethods) entryConsumer.accept(methodDef);
                if (doParameters) for (MutableParameterDef parameterDef : methodDef.getParametersMutable()) {
                    entryConsumer.accept(parameterDef);
                }
            }
            if (doFields) for (MutableFieldDef fieldDef : def.getFieldsMutable()) {
                entryConsumer.accept(fieldDef);
            }
        }
    }
}
