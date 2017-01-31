CAB PARSER
===========

Provides a means to parse and extract data from Microsoft CAB files, from Java.

Specifically, this project was created to extract files from within a .cab which are compressed via the LZX compression algorithm.

Microsoft CAB file format: http://msdn.microsoft.com/en-us/library/bb417343.aspx

- This is for cross-platform use, specifically - linux 32/64, mac 32/64, and windows 32/64. Java 6+


<h4>We now release to maven!</h4> 

This project **includes** some utility classes, which are an extremely small subset of a much larger library; including only what is *necessary* for this particular project to function. Additionally this project is **kept in sync** with the utilities library, so "jar hell" is not an issue, and the latest release will always include the same utility files as all other projects in the dorkbox repository at that time.
  
  Please note that the utility classes have their source code included in the release, and eventually the entire utility library will be provided as a dorkbox repository.
```
<dependency>
  <groupId>com.dorkbox</groupId>
  <artifactId>CabParser</artifactId>
  <version>2.10</version>
</dependency>
```

Or if you don't want to use Maven, you can access the files directly here:  
https://oss.sonatype.org/content/repositories/releases/com/dorkbox/CabParser/  


<h2>License</h2>

This project is © 2012 dorkbox llc, and is distributed under the terms of the Apache v2.0 License. See file "LICENSE" for further references.

