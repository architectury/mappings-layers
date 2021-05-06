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

package me.shedaniel.mappingslayers.impl;

import me.shedaniel.mappingslayers.api.*;
import me.shedaniel.mappingslayers.api.mutable.MappingsEntry;
import me.shedaniel.mappingslayers.api.mutable.MutableMapped;
import me.shedaniel.mappingslayers.api.transform.MappingsTransformation;
import me.shedaniel.mappingslayers.api.transform.SimpleMappingsTransformation;
import me.shedaniel.mappingslayers.api.transform.builtin.MapTransformation;
import me.shedaniel.mappingslayers.api.utils.MappingsUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MappingsTransformationBuilderImpl implements MappingsTransformationBuilder {
    private final List<MappingsTransformation> transformations = new ArrayList<>();
    private final MappingsTransformationContext context;
    private final long epic;
    
    public MappingsTransformationBuilderImpl(MappingsTransformationContext context) {
        this.context = context;
        this.epic = new Random().nextLong();
    }
    
    @Override
    public MappingsTransformationContext getContext() {
        return context;
    }
    
    @Override
    public void add(MappingsTransformation transformation) {
        this.transformations.add(transformation);
    }
    
    @Override
    public void mapClass(String intermediary, String mapped) {
        Objects.requireNonNull(mapped, "mapped is null");
        add(new MapTransformation(MappingsEntryType.CLASS, intermediary, mapped));
    }
    
    @Override
    public void mapMethod(String intermediary, String mapped) {
        Objects.requireNonNull(mapped, "mapped is null");
        add(new MapTransformation(MappingsEntryType.METHOD, intermediary, mapped));
    }
    
    @Override
    public void mapField(String intermediary, String mapped) {
        Objects.requireNonNull(mapped, "mapped is null");
        add(new MapTransformation(MappingsEntryType.FIELD, intermediary, mapped));
    }
    
    @Override
    public void overrideOnly(Mappings mappings, MappingOverridePredicate predicate) {
        // TODO
    }
    
    @Override
    public void replace(Predicate<MappingsEntryType> typePredicate, String regex, String replacement) {
        Objects.requireNonNull(typePredicate, "typePredicate is null");
        Objects.requireNonNull(regex, "regex is null");
        Objects.requireNonNull(replacement, "replacement is null");
        int id = MappingsUtils.getTypeId(typePredicate);
        Pattern pattern = Pattern.compile(regex);
        add(new SimpleMappingsTransformation() {
            @Override
            public void handle(MutableMapped entry) {
                if (entry.isMapped()) {
                    Matcher matcher = pattern.matcher(entry.getMapped());
                    if (matcher.matches()) {
                        entry.setMapped(matcher.replaceAll(replacement));
                    }
                }
            }
            
            @Override
            public boolean handleType(MappingsEntryType type) {
                return typePredicate.test(type);
            }
            
            @Override
            public String uuid() {
                return id + "\0" + regex + "\0" + replacement;
            }
        });
    }
    
    @Override
    public void replace(Predicate<MappingsEntryType> typePredicate, Consumer<MappingsEntry> operator) {
        Objects.requireNonNull(typePredicate, "typePredicate is null");
        Objects.requireNonNull(operator, "operator is null");
        int id = MappingsUtils.getTypeId(typePredicate);
        add(new SimpleMappingsTransformation() {
            @Override
            public void handle(MutableMapped entry) {
                operator.accept(entry);
            }
            
            @Override
            public boolean handleType(MappingsEntryType type) {
                return typePredicate.test(type);
            }
            
            @Override
            public String uuid() {
                return id + epic + operator.getClass().toString();
            }
        });
    }
    
    @Override
    public String uuid() {
        return transformations.stream().map(MappingsTransformation::uuid)
                .collect(Collectors.joining("||||"));
    }
    
    @Override
    public List<MappingsTransformation> getTransformations() {
        return Collections.unmodifiableList(transformations);
    }
}
