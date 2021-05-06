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

import com.google.common.hash.Hashing;
import me.shedaniel.mappingslayers.impl.mappings.TinyTreeMappings;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MappingsReaders {
    private MappingsReaders() {}
    
    public static Mappings readDetection(Path file) {
        try (FileSystem fs = FileSystems.newFileSystem(file, null)) {
            Path tinyPath = fs.getPath("mappings/mappings.tiny");
            if (Files.exists(tinyPath)) {
                byte[] bytes = Files.readAllBytes(tinyPath);
                String uuid = Hashing.sha512().hashBytes(bytes).toString();
                return readTiny(new String(bytes, StandardCharsets.UTF_8), uuid);
            }
            if (Files.exists(fs.getPath("fields.csv")) && Files.exists(fs.getPath("methods.csv"))) {
                return readMCP(fs);
            }
            throw new IllegalArgumentException("Unknown mappings " + file);
        } catch (IOException e) {
            try {
                byte[] bytes = Files.readAllBytes(file);
                String uuid = Hashing.sha512().hashBytes(bytes).toString();
                return readTiny(new String(bytes, StandardCharsets.UTF_8), uuid);
            } catch (IOException ioException) {
                UncheckedIOException exception = new UncheckedIOException(ioException);
                exception.addSuppressed(e);
                throw exception;
            }
        }
    }
    
    public static Mappings readMCP(FileSystem fs) {
        throw new UnsupportedOperationException("MCP is not supported yet!");
    }
    
    public static Mappings readTiny(String content, String uuid) {
        if (content.startsWith("v1")) {
            // OH THIS IS GOING TO SUCK
            System.out.println("Please use a v2 tiny-based mappings file! Field names will not be proposed!");
        }
        try {
            return fromTinyTree(TinyMappingFactory.loadWithDetection(new BufferedReader(new StringReader(content))), uuid);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static Mappings fromTinyTree(TinyTree tinyTree, String uuid) {
        return new TinyTreeMappings(tinyTree, uuid);
    }
}
