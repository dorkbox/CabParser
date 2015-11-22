CAB PARSER
===========

Provides a means to parse and extract data from Microsoft CAB files, from Java.

Specifically, this project was created to extract files from within a .cab which are compressed via the LZX compression algorithm.

Microsoft CAB file format: http://msdn.microsoft.com/en-us/library/bb417343.aspx

- This is for cross-platform use, specifically - linux 32/64, mac 32/64, and windows 32/64. Java 6+

We now release to maven! 

There are two dependencies because we did not want to bake-in a hard dependency into the POM file. We leave this decision to you. 

The included utilities are an extremely small subset of a much larger library, including only what is *necessary* for the CabParser to function. The larger version of the library is *not yet available* but will be as soon as it is converted to maven use. 

This project is kept in sync with the larger library and can be substituted as necessary. Please note that the larger library is not initially listed here as it has **many** dependencies that are not *necessary* for this project. 
```
<dependency>
  <groupId>com.dorkbox</groupId>
  <artifactId>CabParser</artifactId>
  <version>1.1</version>
</dependency>

<dependency>
  <groupId>com.dorkbox</groupId>
  <artifactId>CabParser-Dorkbox-Util</artifactId>
  <version>1.1</version>
</dependency>
```

And You can access the files directly here:
https://oss.sonatype.org/content/repositories/releases/com/dorkbox/CabParser/
https://oss.sonatype.org/content/repositories/releases/com/dorkbox/CabParser-Dorkbox-Util/
