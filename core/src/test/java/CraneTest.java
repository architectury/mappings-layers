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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

public class CraneTest {
    @Test
    public void testCrane() {
        MutableTinyTree mappings = MappingsUtils.deserializeFromString(resource("crane.tiny"));
        System.out.println(mappings);
    }
    
    private String resource(String path) {
        try (InputStreamReader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path))) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
