# svg2ico-gradle-plugin

A Gradle plugin for converting SVG images into ICO and PNG raster images.  Available under the [Apache 2 License](https://www.apache.org/licenses/LICENSE-2.0).

## How to use

### Add the plugin to your build 

Either Kotlin:

```kotlin
plugins {
    id("com.gitlab.svg2ico") version "1.1"
}
```

or Groovy:
```groovy
plugins {
    id 'com.gitlab.svg2ico' version '1.1'
}
```

### Add a task to make an ICO

Kotlin:
```kotlin
tasks.register("ico", com.gitlab.svg2ico.Svg2IcoTask::class) {
    source {
        sourcePath = file("resources/favicon.svg")
    }
    destination = project.layout.buildDirectory.file("icons/favicon.ico")
}
```

Groovy:
```groovy
task ico (type : Svg2IcoTask) {
    source {
        sourcePath = file('resources/favicon.svg')
    }
    destination = project.layout.buildDirectory.file('icons/favicon.ico')
}
```

### Add a task to make a PNG

Kotlin:
```kotlin
tasks.register("png", com.gitlab.svg2ico.Svg2PngTask::class) {
    source = file("resources/favicon.svg")
    width = 128
    height = 128
    destination = project.layout.buildDirectory.file("icons/favicon.png")
}
```

Groovy:
```groovy
task png (type : Svg2PngTask) {
    source = file('resources/favicon.svg')
    width = 128
    height = 128
    destination = project.layout.buildDirectory.file('icons/favicon.png')
}
```

### Stylesheets

Both tasks accept a `userStyleSheet` parameter to specify a stylesheet to apply to the SVG, for example:
```kotlin
tasks.register("ico", com.gitlab.svg2ico.Svg2IcoTask::class) {
    source {
        sourcePath = file("resources/favicon.svg")
        userStyleSheet = file("resources/user.css")
    }
    destination = project.layout.buildDirectory.file("icons/favicon.ico")
}
```

### Refinements to ICO output

An ICO file can contain images at multiple resolutions, allowing the client to pick the most appropriate resolution.  By default, the `svg2ico` task will produce an ICO containing 64 x 64, 48 x 48, 32 x 32, 24 x 24, and 16 x 16 pixel resolutions.

The task supports specifying a different set of resolutions if you want to reduce the file size, or if you know the ICO will be rendered at a particular resolution, for example.

It's also possible to specify different source SVGs for different resolutions, so you can use a detailed source SVG for high resolutions and a simplified one for low resolutions.

The following example makes an ICO with a 64 x 64 image from a detailed SVG, and a 32 x 32 image from a simplified SVG. 
```kotlin
tasks.register("ico", com.gitlab.svg2ico.Svg2IcoTask::class) {
    source {
        sourcePath = file("resources/detailed-favicon.svg")
        output { width = 64; height = 64 }
    }
    source {
        sourcePath = file("resources/simplified-favicon.svg")
        output { width = 32; height = 32 }
    }
    destination = project.layout.buildDirectory.file("icons/favicon.ico")
}
```
## Support

If you find a bug or have a question, [create an issue](https://gitlab.com/svg2ico/svg2ico-gradle-plugin/-/issues).

## Development

Build the project and run the tests with:

```shell
./gradlew check
```

Merge requests very welcome!