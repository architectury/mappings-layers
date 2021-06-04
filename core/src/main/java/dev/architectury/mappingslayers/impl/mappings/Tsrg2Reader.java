package dev.architectury.mappingslayers.impl.mappings;

import dev.architectury.mappingslayers.api.mutable.*;
import dev.architectury.mappingslayers.api.utils.MappingsUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tsrg2Reader {
    public static MutableTinyTree read(Iterator<String> reader) {
        if (!reader.hasNext()) {
            throw new IllegalStateException("Cannot read empty tsrg2!");
        }
        String header = reader.next();
        if (!header.startsWith("tsrg2")) {
            throw new IllegalStateException("Tsrg2 must start with 'tsrg2'!");
        }
        List<String> namespaces = Stream.of(header.split(" ")).skip(1).collect(Collectors.toList());
        MutableTinyTree tree = MappingsUtils.create(MutableTinyMetadata.create(2, 0, namespaces, new HashMap<>()));
        MutableClassDef classDef = null;
        MutableMethodDef methodDef = null;
        while (reader.hasNext()) {
            String line = reader.next();
            int indent = line.lastIndexOf('\t') + 1;
            String[] group = line.substring(indent).split(" ");
            switch (indent) {
                case 0:
                    classDef = tree.getOrCreateClass(group[0]);
                    for (int i = 1; i < group.length; i++) {
                        classDef.setName(i, group[i]);
                    }
                    break;
                case 1:
                    if (group.length == namespaces.size() + 1) {
                        methodDef = classDef.getOrCreateMethod(group[0], group[1]);
                        for (int i = 2; i < group.length; i++) {
                            methodDef.setName(i - 1, group[i]);
                        }
                    } else {
                        MutableFieldDef fieldDef = classDef.getOrCreateField(group[0], "");
                        for (int i = 1; i < group.length; i++) {
                            fieldDef.setName(i, group[i]);
                        }
                    }
                    break;
                case 2:
                    if (group[0].equals("static")) continue;
                    MutableParameterDef parameterDef = methodDef.getOrCreateParameter(Integer.parseInt(group[0]), group[1]);
                    for (int i = 2; i < group.length; i++) {
                        parameterDef.setName(i - 1, group[i]);
                    }
                    break;
            }
        }
        return tree;
    }
}