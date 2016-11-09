# Phon Plug-in Example

An example plug-in for Phon using groovy.  This plug-in adds a menu item
to all Phon windows.

## Source Files

Groovy source files must be added to the folder src/main/groovy.  Java code may be
added to the folder src/main/java.  Other files should be added to src/main/resources.


## Compiling

Compile and package using maven.

```
mvn package
```

After compiling, copy the plug-in jar to the Phon plugins folder.

```
mkdir ~/Documents/Phon/plugins
cp target/phon-phon-groovy-plugin-example-1.jar ~/Documents/Phon/plugins/
```

