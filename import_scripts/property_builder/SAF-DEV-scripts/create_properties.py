#!/usr/bin/env python
# coding: utf-8

# # Notebook to create a new LUX-SAF Wikibase instance
# This script builds on an excel file that is provided by the data modellers. That excel file **TODO** Provide a link to repository containing the latest version(s).
# That file contains the CIDOC-CRM representation of the SAF with mappings to the perceived Wikibase(Qualifier).


from wikidataintegrator import wdi_core, wdi_login, wdi_config
from getpass import getpass
import pandas as pd
import os

wikibase = os.environ["WIKIBASE_HOST"]
api = wikibase+":8080/w/api.php"
sparql = wikibase+":8282/proxy/wdqs/bigdata/namespace/wdq/sparql"
entityUri = "http://mediawiki.svc/entity/"  # this is used to strip it from results!

WBUSER = os.environ["MW_ADMIN_NAME"]
WBPASS = os.environ["MW_ADMIN_PASS"]
login = wdi_login.WDLogin(WBUSER, WBPASS, mediawiki_api_url=api)
localEntityEngine = wdi_core.WDItemEngine.wikibase_item_engine_factory(api, sparql)


# # import data
model_def = pd.read_excel("DM_SAF_vers.1.0.2_andra.xlsx", header=1)
model_def


# # Create properties



def createProperty(login=login, wdprop=None, lulabel="", enlabel="", frlabel="", delabel="", description="", property_datatype=""):
    if wdprop== None:
        s = []
    else:
        s = [wdi_core.WDUrl(wdprop, prop_nr="P1")]
    localEntityEngine = wdi_core.WDItemEngine.wikibase_item_engine_factory(api,sparql)
    item = localEntityEngine(data=s)
    if lulabel != "":
        item.set_label(lulabel, lang="lb")
    item.set_label(enlabel, lang="en")
    item.set_label(delabel, lang="de")
    item.set_label(frlabel, lang="fr")
    item.set_description(description, lang="en")

    print(item.write(login, entity_type="property", property_datatype=property_datatype))


# # OWL properties to capture CIDOC-CRM

## DR: First we create the main properties (These will become P1, P2, etc.)

# instance of
createProperty(login, lulabel="ass eng",
                      enlabel="instance of",
                      frlabel="instance de",
                      delabel="ist ein(e)",
                      property_datatype="wikibase-item")

# subclass of
createProperty(login, lulabel="Ënnerklass vu(n)",
                      enlabel="subclass of",
                      frlabel="sous-classe de",
                      delabel="Unterklasse von",
                      property_datatype="wikibase-item")
# skos:exact match
createProperty(login, lulabel="genauen Match",
                      enlabel="exact match",
                      frlabel="correspondance exacte",
                      delabel="exakte Übereinstimmung",
                      description="mapping",
                      property_datatype="url")
#domain
createProperty(login, lulabel="domain",
                      enlabel="domain",
                      frlabel="domaine",
                      delabel="domain",
                      property_datatype="wikibase-item")
#range
createProperty(login, lulabel="reechwäit",
                      enlabel="range",
                      frlabel="intervalle",
                      delabel="reichweite",
                      property_datatype="wikibase-item")
#subPropertyOf
createProperty(login, lulabel="Ënnerbesëtz vun",
                      enlabel="subproperty of",
                      frlabel="sous-propriété de",
                      delabel="untereigenschaft von",
                      property_datatype="wikibase-item")
#inverseOf
createProperty(login, lulabel="invers vun",
                      enlabel="inverse of",
                      frlabel="inverse de",
                      delabel="invers von",
                      property_datatype="wikibase-item")

## DR: Once they have been created, we fetch them from the WDQS, so that we may learn their prop (e.g. "P1") and corresponding label "e.g. 'instance of'"
## This will be a list like:
##
# prop	propLabel
# http://mediawiki.svc/entity/P1	instance of
# http://mediawiki.svc/entity/P2	subclass of


propertyID = dict()
query = """SELECT ?prop ?propLabel WHERE {
  ?prop wikibase:directClaim ?wdt .
  SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
}"""
for index, row in wdi_core.WDItemEngine.execute_sparql_query(query, as_dataframe = True, endpoint=sparql).iterrows():
    print(row["prop"].replace(entityUri, ""), row["propLabel"])
    propertyID[row["propLabel"]] = row["prop"].replace(entityUri, "")

## DR: Why do we set propertyID? it's never used afterwards

## DR: We additionally create two items named "Class" (Q1) and "Property" (Q2)

# class item
localEntityEngine = wdi_core.WDItemEngine.wikibase_item_engine_factory(api, sparql)
item = localEntityEngine(new_item=True)
item.set_label("Class", lang="en")
item.set_aliases(["Owl:Class"], lang="en")
item.write(login)

# property item
localEntityEngine = wdi_core.WDItemEngine.wikibase_item_engine_factory(api, sparql)
item = localEntityEngine(new_item=True)
item.set_label("Property", lang="en")
item.set_aliases(["owl:ObjectProperty"], lang="en")
item.write(login)


# # Read the property definitions from the DM_SAF

for index, row in model_def.iterrows():
    if row["Data type"].strip() in wdi_config.property_value_types.keys():
        print(row["Data type"])
        try:
            createProperty(
                login,
                enlabel=row["English"],
                frlabel=row["français"],
                delabel=row["Deutsch"],
                description="Lux SAF Property",
                property_datatype=row["Data type"].strip()
            )
        except:
            print("Error with ", row["English"])
    else:
        print("Error", row["Data type"])


# # Create the controlled lists as described in DM_SAF
# ## CL4 Gender

# In[56]:

CL4 = pd.read_excel("DM_SAF_vers.1.0.2_andra.xlsx", sheet_name="CL4 GENDER")
for index, row in CL4.iterrows():
    print(row["Label (English)"])
    item = localEntityEngine(new_item=True)
    item.set_label(row["Label (English)"], lang="en")
    item.set_label(row["Label (German)"], lang="de")
    item.set_label(row["Label (French)"], lang="fr")
    print(item.write(login))


# ## CL5 STATUS

# In[57]:


CL5 = pd.read_excel("DM_SAF_vers.1.0.2_andra.xlsx", sheet_name="CL5 STATUS")
for index, row in CL5.iterrows():
    print(row["Label (English)"])
    item = localEntityEngine(new_item=True)
    item.set_label(row["Label (English)"], lang="en")
    item.set_label(row["Label (German)"], lang="de")
    item.set_label(row["Label (French)"], lang="fr")
    print(item.write(login))


# In[58]:


CL3 = pd.read_excel("DM_SAF_vers.1.0.2_andra.xlsx", sheet_name="CL3 Name Format")
for index, row in CL3.iterrows():
    item = localEntityEngine(new_item=True)
    item.set_label(row["Cataloging specs"])
    print(item.write(login))


# # manually added external identifiers not yet covered in DMG

# In[59]:


#ARK
createProperty(login, lulabel="ARK",
                      enlabel="ARK",
                      frlabel="ARK",
                      delabel="ARK",
                      property_datatype="url")


person_item = localEntityEngine(new_item=True)
person_item.set_label("E21 Person", lang="en")
print(person_item.write(login))




