# Mappings Layers

A gradle plugin that allows modification of tiny mappings.
___

## Usage

```groovy
plugins {
    id "me.shedaniel.mappings-layers-plugin" version "1.0-SNAPSHOT"
}

dependencies {
    mappings mappingsLayers.from("net.fabricmc:yarn:1.16.5+build.4:v2") {
        it.mapClass("net/minecraft/class_310", "net/minecraft/client/MinecraftCorb")
        it.mapMethod("method_23182", "renderRandomStuffPlease")
        it.mapField("field_1700", "theMinecraftClientCorbLmao")
    }
}
```

## License

Lesser General Public License v3