CAB PARSER
===========

Provides a means to parse and extract data from Microsoft CAB files, from Java.

Specifically, this project was created to extract files from within a .cab which are compressed via the LZX compression algorithm.

Microsoft CAB file format: http://msdn.microsoft.com/en-us/library/bb417343.aspx

- This is for cross-platform use, specifically - linux 32/64, mac 32/64, and windows 32/64. Java 6+


&nbsp; 
&nbsp; 

Release Notes 
---------

This project includes some utility classes that are a small subset of a much larger library. These classes are **kept in sync** with the main utilities library, so "jar hell" is not an issue, and the latest release will always include the same version of utility files as all of the other projects in the dorkbox repository at that time. 
  
  Please note that the utility source code is included in the release and on our [Git Server](https://git.dorkbox.com/dorkbox/Utilities) repository.
  
  
Maven Info
---------
```
<dependencies>
    ...
    <dependency>
      <groupId>com.dorkbox</groupId>
      <artifactId>CabParser</artifactId>
      <version>2.15</version>
    </dependency>
</dependencies>
```

  
Gradle Info
---------
````
dependencies {
    ...
    compile 'com.dorkbox:CabParser:2.15'
}
````


Or if you don't want to use Maven, you can access the files directly here:  
https://repo1.maven.org/maven2/com/dorkbox/CabParser/  


License
---------
This project is © 2012 dorkbox llc, and is distributed under the terms of the Apache v2.0 License. See file "LICENSE" for further references.

