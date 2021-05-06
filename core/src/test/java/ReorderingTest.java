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

import dev.architectury.mappingslayers.api.mutable.MutableTinyTree;
import dev.architectury.mappingslayers.api.utils.MappingsUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;

public class ReorderingTest {
    @Test
    public void testReordering() {
        MutableTinyTree mappings = MappingsUtils.deserializeFromString(resource("simple.tiny"));
        mappings = MappingsUtils.reorderNamespaces(mappings, Arrays.asList("named", "intermediary", "official"));
        MappingsUtils.sort(mappings, "official");
        
        // simple-reordered.tiny is a reordered file using stitch
        MutableTinyTree actual = MappingsUtils.deserializeFromString(resource("simple-reordered.tiny"));
        MappingsUtils.sort(actual, "official");
        Assertions.assertEquals(MappingsUtils.serializeToString(mappings), MappingsUtils.serializeToString(actual));
    }
    
    @Test
    public void testStripping() {
        MutableTinyTree mappings = MappingsUtils.deserializeFromString(resource("simple.tiny"));
        mappings = MappingsUtils.removeNamespaces(mappings, "official");
        System.out.println(MappingsUtils.serializeToString(mappings));
    }
    
    private String resource(String path) {
        try (InputStreamReader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path))) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
