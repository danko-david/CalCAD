# CalCAD

A library to procedurally generate 3D models for 3D printing.

The motivation of this software is that I can't properly use 3D modelling 
softwares like Blender, FreeCAD and i find procedural generation more obvious
and reuseable than carving structures by hand.

The need of a new software is also driven by the need of creating an open
library that can be used to create cusom object builder utilities for specific
build purpose.


## Influence of other softwares

This software is based on the need of procedural generation of 3D designs,
influenced by OpenSCAD, based on JFXScad and uses the jcsg library as JFXScad does.


### [OpenSCAD](https://www.openscad.org/)
When i first used OpenSCAD i thought it is the proper tool for my task.
So i implemented the [Metrix block builder](./src/main/java/eu/javaexperience/calcad/examples/metrix/block.scad)
and realised is that preview is generated fast but generating the actual
printalbe model is terribly slow (3 secounds relative to 1 hour).
I also implemented [Metrix block builder](./src/main/java/eu/javaexperience/calcad/examples/metrix/MetrixBlockBuilder.java) in this system.

OpenSCAD pros:
- ready to use software
- rich features (custom language, hull, extrusion)
- widely used 3D procedural generation method (Used eg on [https://www.thingiverse.com/](https://www.thingiverse.com/))

OpenSCAD cons:
- Slow STL rendering
- no measuring functions, sizes can't be readed from the models.


### [JFXScad](https://github.com/miho/JFXScad)

After facing the limitation of OpenSCAD, i was looked for other CSG alternative
and found this software. It is written in java.

Pros:
- The creator of the software is also created the CSG library which is use in this project.
- Ready to use software, with nice simple GUI.

Cons:
- Reduced functionality relative to OpenSCAD.
- A standalone application that uses [apache groovy](https://groovy-lang.org/) without proper source editor (eg auto completition, compilation error report).
- Difficult to build and make to run. (Maybe it's just my difficulties)

## Improvements and regressions

Improvements:
- This software is a library, you can include to your project. (OpenSCAD/JFXScad)
- Multithreaded CSG rendering (OpenSCAD/JFXScad)
- Binary STL format (JFXScad)

Regressions:
- missing advanced functions: polygon functions, text, extrusion

## Usage

### Common steps

Add this project as dependency. (It's currently located in a custom maven repository)

```xml
<project ...>
...
	<repositories>
		<repository>
			<id>jvx-repo</id>
			<name>Javaexprience-custom-repo</name>
			<url>https://maven.javaexperience.eu/</url>
		</repository>
	</repositories>

	<dependencies>
		...
		<dependency>
			<groupId>javaexperience</groupId>
			<artifactId>calcad</artifactId>
			<version>1.0</version>
		</dependency>
	</dependencies>
...
</project>
```

### Use for developement with hot code replace

For example, see [CalCadMetrixExample](./src/main/java/eu/javaexperience/calcad/examples/metrix/CalCadMetrixExample.java) class.

If your IDE supports hot code replace, you can modify the code in a running JVM.
Choosing this way of developement, you can start a JVM, change the code, then the
"Refresh" button you can see the result of your modificatons.

Hint: use `import static eu.javaexperience.calcad.lib.Cal.*;` to use shorthand static function like `union(...)` instead of `Cal.union(...)`

## Known bugs

### Broken surface
Problem:
If you generates object has drilled holes, some of the surface of the holes in
slic3r appears to be broken, and some layer of the object is generated bad.

Details:
It's happens only in slic3r. It's not appears in [meshlab](https://www.meshlab.net/), [Blender](https://www.blender.org/) or [Cura](https://ultimaker.com/software/ultimaker-cura).
I thought it's because the naive triangualtion method used in CSG.toStlString(), but i implemented triangulation with [Earclipping](https://github.com/perthcpe23/earcut-j), but nothing changed.
This bug also appears in JFXScad.

Temporary solution 'till fix:
Use cura instead of Slic3r.


