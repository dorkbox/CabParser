CAB PARSER
===========

###### [![Dorkbox](https://badge.dorkbox.com/dorkbox.svg "Dorkbox")](https://git.dorkbox.com/dorkbox/CabParser) [![Github](https://badge.dorkbox.com/github.svg "Github")](https://github.com/dorkbox/CabParser) [![Gitlab](https://badge.dorkbox.com/gitlab.svg "Gitlab")](https://gitlab.com/dorkbox/CabParser)



Provides a means to parse and extract data from Microsoft CAB files, from Java.

Specifically, this project was created to extract files from within a .cab which are compressed via the LZX compression algorithm.

Microsoft CAB file format: http://msdn.microsoft.com/en-us/library/bb417343.aspx

- This is for cross-platform use, specifically - linux 32/64, mac 32/64, and windows 32/64. Java 8+



&nbsp; 
&nbsp; 

Maven Info
---------
```
<dependencies>
    ...
    <dependency>
      <groupId>com.dorkbox</groupId>
      <artifactId>CabParser</artifactId>
      <version>3.3</version>
    </dependency>
</dependencies>
```

Gradle Info
---------
```
dependencies {
    ...
    implementation("com.dorkbox:CabParser:3.3")
}
```

License
---------
This project is © 2023 dorkbox llc, and is distributed under the terms of the Apache v2.0 License. See file "LICENSE" for further 
references.
