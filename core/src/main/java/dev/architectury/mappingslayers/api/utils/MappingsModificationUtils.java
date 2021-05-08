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

package dev.architectury.mappingslayers.api.utils;

import dev.architectury.mappingslayers.api.mutable.MutableTinyTree;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.UnaryOperator;

public final class MappingsModificationUtils {
    private MappingsModificationUtils() {}
    
    public static void modify(Path file, UnaryOperator<MutableTinyTree> operator) throws IOException {
        modify(file, file, operator);
    }
    
    public static void modify(Path input, Path output, UnaryOperator<MutableTinyTree> operator) throws IOException {
        String newContent = modify(new String(Files.readAllBytes(input), StandardCharsets.UTF_8), operator);
        Path parent = output.getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.write(output, newContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
    }
    
    public static String modify(String content, UnaryOperator<MutableTinyTree> operator) {
        MutableTinyTree tree = MappingsUtils.deserializeFromString(content);
        tree = operator.apply(tree);
        return MappingsUtils.serializeToString(tree);
    }
}
