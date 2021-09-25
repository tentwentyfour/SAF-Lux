#!/usr/bin/env python
# coding: utf-8

from wikidataintegrator import wdi_core, wdi_login, wdi_config
from getpass import getpass
import pandas as pd
import os

wbstack = os.environ["WBSTACK"]
wikibase = "https://{}.wiki.opencura.com/".format(wbstack)
api = "https://{}.wiki.opencura.com/w/api.php".format(wbstack)
sparql = "https://{}.wiki.opencura.com/query/sparql".format(wbstack)
entityUri = wikibase.replace("https:", "http:")+"entity/"
WBUSER = os.environ["MW_ADMIN_NAME"]
WBPASS = os.environ["MW_ADMIN_PASS"]
login = wdi_login.WDLogin(WBUSER, WBPASS, mediawiki_api_url=api)
localEntityEngine = wdi_core.WDItemEngine.wikibase_item_engine_factory(api,sparql)

model_def = pd.read_excel("../../DM_SAF/DM_SAF_vers.1.0.3_andra.xls", header=1)

def createProperty(login=login, wdprop=None, lulabel="", enlabel="", frlabel="", delabel="", description="", property_datatype=""):
    if wdprop== None:
        s = []
    else:
        s = [wdi_core.WDUrl(wdprop, prop_nr="P1")]
    localEntityEngine = wdi_core.WDItemEngine.wikibase_item_engine_factory(api,sparql)
    item = localEntityEngine(data=s, core_props=set())
    if lulabel != "":
        item.set_label(lulabel, lang="lb")
    item.set_label(enlabel, lang="en")
    item.set_label(delabel, lang="de")
    item.set_label(frlabel, lang="fr")
    item.set_description(description, lang="en")
    print(item.write(login, entity_type="property", property_datatype=property_datatype))

# instance of
createProperty(login, lulabel="ass eng",
                      enlabel="instance of",
                      frlabel="instance de",
                      delabel="ist ein(e)",
                      property_datatype="wikibase-item")

# subclass of
##
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

#property
createProperty(login, enlabel="property",
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

for index, row in model_def.iterrows():
    if row["Data type"].strip() in wdi_config.property_value_types.keys():
        print(row["Data type"])
        try:
            createProperty(login, enlabel=row["English"], frlabel=row["français"], delabel=row["Deutsch"], description="Lux SAF Property", property_datatype=row["Data type"].strip())
        except:
            print("Error with ", row["English"])
    else:
        print("Error", row["Data type"])

## Items
# class item
item = localEntityEngine(new_item=True, core_props=set())
item.set_label("Class", lang="en")
item.set_aliases(["Owl:Class"], lang="en")
print(item.write(login))

# property item
item = localEntityEngine(new_item=True, core_props=set())
item.set_label("Property", lang="en")
item.set_aliases(["owl:ObjectProperty"], lang="en")
print(item.write(login))

CL4 = pd.read_excel("../../DM_SAF/DM_SAF_vers.1.0.3_andra.xls", sheet_name="CL4 GENDER")
for index, row in CL4.iterrows():
    print(row["Label (English)"])
    item = localEntityEngine(new_item=True, core_props=set())
    item.set_label(row["Label (English)"], lang="en")
    item.set_label(row["Label (German)"], lang="de")
    item.set_label(row["Label (French)"], lang="fr")
    print(item.write(login))

CL5 = pd.read_excel("../../DM_SAF/DM_SAF_vers.1.0.3_andra.xls", sheet_name="CL5 STATUS")
for index, row in CL5.iterrows():
    print(row["Label (English)"])
    item = localEntityEngine(new_item=True)
    item.set_label(row["Label (English)"], lang="en")
    item.set_label(row["Label (German)"], lang="de")
    item.set_label(row["Label (French)"], lang="fr")
    print(item.write(login))

CL3 = pd.read_excel("../../DM_SAF/DM_SAF_vers.1.0.3_andra.xls", sheet_name="CL3 Name Format")
for index, row in CL3.iterrows():
    item = localEntityEngine(new_item=True, core_props=set())
    item.set_label(row["Cataloging specs"])
    print(item.write(login))

CL8 =  pd.read_excel("../../DM_SAF/DM_SAF_vers.1.0.3_andra.xls", sheet_name="CL8 INTERNAL IDENTIFIER")
for index, row in CL8.iterrows():
    print(row["Label "])
    createProperty(login, lulabel=row["Label "].strip(), 
                      enlabel=row["Label "].strip(),
                      frlabel=row["Label "].strip(),
                      delabel=row["Label "].strip(),
                      property_datatype="external-id")

#ARK
createProperty(login, lulabel="ARK", 
                      enlabel="ARK",
                      frlabel="ARK",
                      delabel="ARK",
                      property_datatype="url")


person_item = localEntityEngine(new_item=True, core_props=set())
person_item.set_label("E21 Person", lang="en")
print(person_item.write(login))




