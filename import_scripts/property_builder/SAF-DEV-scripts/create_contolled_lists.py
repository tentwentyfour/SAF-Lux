#!/usr/bin/env python
# coding: utf-8

from wikidataintegrator import wdi_core, wdi_login, wdi_config
from getpass import getpass
import pandas as pd
import os
import sys

wikibase = os.environ["WIKIBASE_HOST"]
api = wikibase+":8080/w/api.php"
sparql = wikibase+":9999/bigdata/namespace/wdq/sparql"
entityUri = wikibase.replace("https:", "http:")+"entity/"
# alternative
## sparql = os.environ["WDQS_HOST"]

WBUSER = os.environ["MW_ADMIN_NAME"]
WBPASS = os.environ["MW_ADMIN_PASS"]
login = wdi_login.WDLogin(WBUSER, WBPASS, mediawiki_api_url=api)
localEntityEngine = wdi_core.WDItemEngine.wikibase_item_engine_factory(api,sparql)

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
    item = localEntityEngine(new_item=True, core_props=set())
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

person_item = localEntityEngine(new_item=True, core_props=set())
person_item.set_label("E21 Person", lang="en")
print(person_item.write(login))




