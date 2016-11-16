# Graffiti - a jar file dependency generator #

Graffiti is a java library for generating relationships betwwen the classes in a jar file. Graffiti helps you bring the relationships to life using visual representations. It generates bootsrap based html files containing the visual representation of the relationships among the classes. It also generates many other useful statistics about the jar like :

- Information about maven dependencies
- Number of classes; interfaces; abstract classes
- Required java version
- Attributes and Methos level details

**It is NOT a UML diagram modelling tool**

## Features ##

* Writte in core java
* Generates beautiful Bootstrap html files.
* Generates individual dot files as well for directed graphs.
* Easy to use.
* Works on Mac, Linux and Windows


## Installation ##

There are two versions of graffiti available. The [WebVersion](http://graffiti.gaurs.io) allows you to upload your jar file and without any hassle it generates the output which will be available for download as a zip package. All you need to do is to browse to the index.html of the extracted package and open it in a browser.

The other option is to download the graffiti package from github repository and build the same using java 1.8 version. This requires the following pre-requisites to be available on your machine :

- [java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [graphviz 2.8 api](http://www.graphviz.org/Download.php)
- [maven](https://maven.apache.org/download.cgi)

After the download please follow the following instructions to build and run graffiti on your machine :
````
$ cd /path/to/downloaded/graffiti
$ java -jar graffiti-core-0.0.1.jar /path/to/jar/to/analyse /path/to/output/dir /path/to/dot/executable
````

## Output##
The output for both the web based version and standalone version is a collection of bootstrap based html files that contains the required informaion about the jar file. In case of the web based version, the same is packaged as a zip file and is available instantaneously for download. The basic structure of the output directory is as follows :
````
.
├── index.html and other html files
├── css
├── dot
├── images
└── js
````
