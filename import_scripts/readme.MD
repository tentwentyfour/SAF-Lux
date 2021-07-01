# Import scripts
This folder contains import scripts to build a Wikibase to host Shared Authority Files bas on CIDOC-CRM. There are two jupyter notebooks:
1. Script [create_properties.ipynb](property_builder/create_properties.ipynb). This first notebook starts from a freshly installed Wikibase. It installs properties to capture and express OWL properties. Then it fetches the properties definitions from the DM-SAF worksheet and creates wikibase properties with their respective data types on the subject wikibase. Finally, Wikibase items are created for the items in the different controlled lists also defined in the DM-SAF spreadsheet.
2. Script [import_cidoc_crm.ipynb](https://github.com/weso/SAF-Lux/blob/main/import_scripts/CIDOC-CRM-properties/import_cidoc_crm.ipynb) is to create a CIDOC-CRM representation in Wikibase. This is achieved by importing the CIDOC-CRM definitions through an OWL file, where the CIDOC-CRM properties and classes are expressed as Wikibase items. 

* TODO: Add mappings between the properties created  in script (2) and the CIDOC CRM definitions imported by [script]([script](https://github.com/weso/SAF-Lux/blob/main/import_scripts/CIDOC-CRM-properties/option2.ipynb))
* TODO: Write a script to validate the CIDOC-CRM relations as expressed in the DM-SAF with the imported CIDOC-CRM definitions. 
