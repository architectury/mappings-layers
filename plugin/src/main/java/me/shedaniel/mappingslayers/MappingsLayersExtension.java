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

package me.shedaniel.mappingslayers;

import com.google.common.hash.Hashing;
import me.shedaniel.mappingslayers.api.Mappings;
import me.shedaniel.mappingslayers.api.MappingsReaders;
import me.shedaniel.mappingslayers.api.MappingsTransformationBuilder;
import me.shedaniel.mappingslayers.impl.MappingsTransformationBuilderImpl;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MappingsLayersExtension {
    private final Project project;
    private final Configuration configuration;
    private final Path cacheFolder;
    
    public MappingsLayersExtension(Project project) {
        this.project = project;
        this.configuration = this.project.getConfigurations().maybeCreate("mappingsLayersInternalConfiguration");
        this.cacheFolder = project.getGradle().getGradleUserHomeDir().toPath().resolve("caches/architectury-mappings-layers");
        try {
            Files.createDirectories(cacheFolder);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public Dependency from(Object notation) {
        return from(notation, null);
    }
    
    public Dependency from(Object notation, Action<MappingsTransformationBuilder> action) {
        Mappings mappings = resolveObject(notation);
        String uuid = mappings.uuid();
        MappingsTransformationBuilderImpl builder = new MappingsTransformationBuilderImpl(this::resolveObject);
        if (action != null) {
            action.execute(builder);
        }
        uuid += "||||||||||";
        uuid += builder.uuid();
        String epic = Hashing.sha256().hashBytes(uuid.getBytes(StandardCharsets.UTF_16)).toString();
        Path resolve = cacheFolder.resolve("mappings/" + epic + "-1.0.0.jar");
        if (!Files.exists(resolve)) {
            try {
                Files.createDirectories(resolve.getParent());
                mappings = mappings.withTransformations(builder.getTransformations());
                Map<String, String> env = new HashMap<>();
                env.put("create", "true");
                URI uri = new URI("jar:" + resolve.toUri());
                try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
                    Files.createDirectories(fs.getPath("mappings"));
                    Files.write(fs.getPath("mappings/mappings.tiny"),
                            mappings.serializeToTiny().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
                }
            } catch (IOException | URISyntaxException e) {
                try {
                    Files.deleteIfExists(resolve);
                } catch (IOException ignored) {}
                throw new RuntimeException(e);
            }
        }
    
        project.getRepositories().flatDir(repository -> repository.dir(resolve.getParent().toFile()));
        return project.getDependencies().create("mappingslayers:" + epic + ":1.0.0");
    }
    
    private Mappings resolveObject(Object notation) {
        if (notation instanceof Mappings) return (Mappings) notation;
        Dependency dependency = project.getDependencies().add(configuration.getName(), notation);
        Set<File> dependencyFiles = configuration.files(dependency);
        if (dependencyFiles.size() != 1) throw new AssertionError("Expecting only 1 file!");
        return MappingsReaders.readDetection(dependencyFiles.iterator().next());
    }
}
