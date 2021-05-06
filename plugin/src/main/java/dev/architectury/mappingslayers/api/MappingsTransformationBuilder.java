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

package dev.architectury.mappingslayers.api;

import dev.architectury.mappingslayers.api.mutable.MappingsEntry;
import dev.architectury.mappingslayers.api.transform.MappingsTransformation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MappingsTransformationBuilder {
    MappingsTransformationContext getContext();
    
    void add(MappingsTransformation transformation);
    
    /**
     * Adds / replaces a mapping of a class.
     *
     * @param intermediary the intermediary name of the class, for example
     *                     {@code net/minecraft/class_310}
     * @param mapped       the mapped name of the class
     */
    void mapClass(String intermediary, String mapped);
    
    /**
     * Adds / replaces a mapping of a method.
     *
     * @param intermediary the intermediary name of the method, for example
     *                     {@code method_23182}
     * @param mapped       the mapped name of the method
     */
    void mapMethod(String intermediary, String mapped);
    
    /**
     * Adds / replaces a mapping of a field.
     *
     * @param intermediary the intermediary name of the field, for example
     *                     {@code field_1700}
     * @param mapped       the mapped name of the field
     */
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
    
    default void overrideOnly(Object notation, MappingOverridePredicate predicate) {
        overrideOnly(getContext().resolveMappings(notation), predicate);
    }
    
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
    
    /**
     * Replaces mapped mappings via regex.
     *
     * @param typePredicate the type predicate of the {@link MappingsEntry}
     * @param regex         the regex to match for
     * @param replacement   the replacement
     */
    void replace(Predicate<MappingsEntryType> typePredicate, String regex, String replacement);
    
    default void replace(MappingsEntryType t, String regex, String replacement) {
        replace(type -> type == t, regex, replacement);
    }
    
    default void replace(String regex, String replacement) {
        replace(type -> true, regex, replacement);
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
    
    List<MappingsTransformation> getTransformations();
}