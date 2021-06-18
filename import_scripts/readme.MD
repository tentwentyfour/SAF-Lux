# Import scripts
This folder contains import scripts to build a Wikibase to host Shared Authority Files. There are two parts to this [script](https://github.com/weso/SAF-Lux/blob/main/import_scripts/CIDOC-CRM-properties/option2.ipynb) is to create a CIDOC-CRM representation in Wikibase. This is achieved by importing the CIDOC-CRM definitions through an OWL file, where the OWL properties are minted as Wikibase properties and the CIDOC-CRM properties and classes are expressed as Wikibase items. 
(2) The second script imports mappings expressed in the DMSAF excel sheet as Wikibase properties. 

* TODO: Add mappings between the properties created  in script (2) and the CIDOC CRM definitions imported by [script]([script](https://github.com/weso/SAF-Lux/blob/main/import_scripts/CIDOC-CRM-properties/option2.ipynb))
* TODO: Write a script to validate the CIDOC-CRM relations as expressed in the DM-SAF with the imported CIDOC-CRM definitions. 
