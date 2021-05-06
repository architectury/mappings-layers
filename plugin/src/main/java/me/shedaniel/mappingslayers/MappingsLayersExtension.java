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
import groovy.lang.Closure;
import me.shedaniel.mappingslayers.api.Mappings;
import me.shedaniel.mappingslayers.api.MappingsReaders;
import me.shedaniel.mappingslayers.api.MappingsTransformationBuilder;
import me.shedaniel.mappingslayers.api.MappingsTransformationContext;
import me.shedaniel.mappingslayers.impl.MappingsTransformationBuilderImpl;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.jetbrains.annotations.Nullable;

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

public class MappingsLayersExtension implements MappingsTransformationContext {
    private final Project project;
    private final Configuration configuration;
    private final Path cacheFolder;
    
    public MappingsLayersExtension(Project project) {
        this.project = project;
        this.configuration = this.project.getConfigurations().detachedConfiguration();
        this.cacheFolder = project.getGradle().getGradleUserHomeDir().toPath().resolve("caches/architectury-mappings-layers");
        try {
            Files.createDirectories(cacheFolder);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public Dependency from(Object notation) {
        return from(notation, (Action<MappingsTransformationBuilder>) null);
    }
    
    public Dependency from(Object notation, @Nullable Closure<MappingsTransformationBuilder> closure) {
        return createMappingsDependency(resolveMappings(notation), builder(toAction(closure)));
    }
    
    public Dependency from(Object notation, @Nullable Action<MappingsTransformationBuilder> action) {
        return createMappingsDependency(resolveMappings(notation), builder(action));
    }
    
    public MappingsTransformationBuilder builder() {
        return builder((Action<MappingsTransformationBuilder>) null);
    }
    
    public MappingsTransformationBuilder builder(@Nullable Closure<MappingsTransformationBuilder> closure) {
        return builder(toAction(closure));
    }
    
    public MappingsTransformationBuilder builder(@Nullable Action<MappingsTransformationBuilder> action) {
        MappingsTransformationBuilderImpl builder = new MappingsTransformationBuilderImpl(this);
        if (action != null) {
            action.execute(builder);
        }
        return builder;
    }
    
    public Dependency createMappingsDependency(Mappings mappings) {
        return createMappingsDependency(mappings, null);
    }
    
    public Dependency createMappingsDependency(Mappings mappings, @Nullable MappingsTransformationBuilder builder) {
        String uuid = mappings.uuid();
        if (builder != null) {
            uuid += "||||||||||";
            uuid += builder.uuid();
        }
        String sha256 = Hashing.sha256().hashBytes(uuid.getBytes(StandardCharsets.UTF_16)).toString();
        Path resolve = cacheFolder.resolve("mappings/" + sha256 + "-1.0.0.jar");
        if (!Files.exists(resolve)) {
            try {
                Files.createDirectories(resolve.getParent());
                if (builder != null) {
                    mappings = mappings.withTransformations(builder.getTransformations());
                }
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
                } catch (IOException ignored) {
                }
                throw new RuntimeException(e);
            }
        }
        
        project.getRepositories().flatDir(repository -> repository.dir(resolve.getParent().toFile()));
        return project.getDependencies().create("mappingslayers:" + sha256 + ":1.0.0");
    }
    
    @Override
    public Mappings resolveMappings(Object o) {
        if (o instanceof Mappings) return (Mappings) o;
        if (o instanceof Dependency) {
            Set<File> dependencyFiles = project.getConfigurations().detachedConfiguration((Dependency) o).getFiles();
            if (dependencyFiles.size() != 1) throw new AssertionError("Expecting only 1 file!");
            return MappingsReaders.readDetection(dependencyFiles.iterator().next().toPath());
        }
        Dependency dependency = project.getDependencies().add(configuration.getName(), o);
        Set<File> dependencyFiles = configuration.files(dependency);
        if (dependencyFiles.size() != 1) throw new AssertionError("Expecting only 1 file!");
        return MappingsReaders.readDetection(dependencyFiles.iterator().next().toPath());
    }
    
    @Nullable
    private static <T> Action<T> toAction(@Nullable Closure<T> closure) {
        if (closure == null) return null;
        return obj -> {
            Closure<T> clone = (Closure<T>) closure.clone();
            clone.setResolveStrategy(1);
            clone.setDelegate(obj);
            clone.call(obj);
        };
    }
}
