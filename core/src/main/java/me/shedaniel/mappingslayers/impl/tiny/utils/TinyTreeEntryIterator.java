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

package me.shedaniel.mappingslayers.impl.tiny.utils;

import me.shedaniel.mappingslayers.api.MappingsEntryType;
import me.shedaniel.mappingslayers.api.mutable.*;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class TinyTreeEntryIterator {
    private TinyTreeEntryIterator() {}
    
    public static void iterate(MutableTinyTree tree, Predicate<MappingsEntryType> typePredicate, Consumer<MutableMapped> entryConsumer) {
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
