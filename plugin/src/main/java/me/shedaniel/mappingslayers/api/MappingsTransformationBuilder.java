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

package me.shedaniel.mappingslayers.api;

import me.shedaniel.mappingslayers.api.mutable.MappingsEntry;
import me.shedaniel.mappingslayers.api.transform.MappingsTransformation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MappingsTransformationBuilder {
    void add(MappingsTransformation transformation);
    
    void mapClass(String intermediary, String mapped);
    
    void mapMethod(String intermediary, String mapped);
    
    void mapField(String intermediary, String mapped);
    
    default void unmapClass(String intermediary) {
        mapClass(intermediary, "");
    }
    
    default void unmapMethod(String intermediary) {
        mapMethod(intermediary, "");
    }
    
    default void unmapField(String intermediary) {
        mapField(intermediary, "");
    }
    
    void overrideOnly(Mappings mappings, MappingOverridePredicate predicate);
    
    void overrideOnly(Object notation, MappingOverridePredicate predicate);
    
    default void overrideAll(Mappings mappings) {
        overrideOnly(mappings, (entry, replaced) -> true);
    }
    
    default void overrideAll(Object notation) {
        overrideOnly(notation, (entry, replaced) -> true);
    }
    
    default void overrideMissing(Mappings mappings) {
        overrideOnly(mappings, (entry, replaced) -> entry == null);
    }
    
    default void overrideMissing(Object notation) {
        overrideOnly(notation, (entry, replaced) -> entry == null);
    }
    
    void replace(Predicate<MappingsEntryType> typePredicate, Consumer<MappingsEntry> operator);
    
    default void replace(Consumer<MappingsEntry> operator) {
        replace(type -> true, operator);
    }
    
    default void replaceMapped(Predicate<MappingsEntryType> typePredicate, Function<@Nullable String, String> operator) {
        replace(typePredicate, entry -> {
            String s = operator.apply(entry.getMappedNullable());
            entry.setMapped(s == null ? "" : s);
        });
    }
    
    default void replaceMapped(MappingsEntryType type, Function<@Nullable String, String> operator) {
        replaceMapped(t -> t == type, operator);
    }
    
    default void replaceMapped(Function<@Nullable String, String> operator) {
        replaceMapped(type -> true, operator);
    }
    
    String uuid();
}