CAB PARSER
===========

Provides a means to parse and extract data from Microsoft CAB files, from Java.

Specifically, this project was created to extract files from within a .cab which are compressed via the LZX compression algorithm.

Microsoft CAB file format: http://msdn.microsoft.com/en-us/library/bb417343.aspx

- This is for cross-platform use, specifically - linux 32/64, mac 32/64, and windows 32/64. Java 6+


<h4>We now release to maven!</h4> 

There is a hard dependency in the POM file for the utilities library, which is an extremely small subset of a much larger library; including only what is *necessary* for this particular project to function.

This project is **kept in sync** with the utilities library, so "jar hell" is not an issue. Please note that the util library (in it's entirety) is not added since there are **many** dependencies that are not *necessary* for this project. No reason to require a massive amount of dependencies for one or two classes/methods.  
```
<dependency>
  <groupId>com.dorkbox</groupId>
  <artifactId>CabParser</artifactId>
  <version>2.9</version>
</dependency>
```

Or if you don't want to use Maven, you can access the files directly here:  
https://oss.sonatype.org/content/repositories/releases/com/dorkbox/CabParser/  
https://oss.sonatype.org/content/repositories/releases/com/dorkbox/CabParser-Dorkbox-Util/  


<h2>License</h2>

This project is © 2012 dorkbox llc, and is distributed under the terms of the Apache v2.0 License. See file "LICENSE" for further references.

