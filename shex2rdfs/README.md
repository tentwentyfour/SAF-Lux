# shex2rdfs

This code generates a simple RDF Schema file that describes the main properties defined in the 
 [ShEx](https://github.com/weso/SAF-Lux/tree/main/shex) following the annotations in that file. 

 The goal of generating an RDF Schema is to follow best practices of semantic web where an RDF Schema vocabulary is defined under a namespace.

 The idea is to keep the ShEx as the main source of truth for the data model, and generate 
  the vocabulary from it.