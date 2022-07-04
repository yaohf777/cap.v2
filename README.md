PoC for **oDataV2** based on CAP


## Application Programming Model
The [BTP application programming model](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/00823f91779d4d42aa29a498e0535cdf.html) is followed.

* UI5 is used in the user interface layer
* Java is used in the service layer. [SAP Cloud SDK](https://mvnrepository.com/artifact/com.sap.cloud.servicesdk.prov/projects-parent) is utilized to provide oData V4 service.
* HANA DB and HANA Core Data Service (CDS) is used in the data layer

## Development Guideline

1. [SAP WebIDE Full-Stack](https://blogs.sap.com/2017/10/23/sap-web-ide-ninja-4-develop-full-stack-to-do-app-in-sap-web-ide-part-1-database/) is the default development IDE.
   
2. [HANA CDS](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/855e00bd559742a3b8276fbed4af1008.html) is used to model database artifacts and HDI (HANA Deployment Infrastructure) is used to manage the database design-time artifacts and the corresponding deployed run-time (catalog) objects. The data model can be edited in **db/src/data_model.hdbcds**, you can then activate and view the database artifacts in the WebIDE Full-Stack.
 
   **Note** that the guidelines contained in data_model.hdbcds need to be followed.
  
3. For service layer, several Java technologys and frameworks are used.
   
   To provide **oData service**, srv/src/main/resources/edmx/**RCService.xml** need to be edited according to HANA data model changed in the previous step. Each oData entity should have one service implementation class in package com.sap.rc.main.service. More notes can be found in BaseService.java file.
   
   Once the service layer code change is finished, you can start the Java service instance by selecting the `srv` folder and click <kbd>Run</kbd>. Once the application is started, you can open the instance through the service URL in the WebIDE.

   **Note** that the service URL is generated based on space and user so that each developers can deploy independent Java instances at the same time to prevent unnecessary mutual interference. However the service instance is referred by the UI layer via application router, it's necessary to change the approuter **srv-dest** URL properties in mta.yaml to map to your specific service URL. **Do not** commit the mta.yaml file if you change it only for this purpose.
```
properties:
  XSAPPNAME: cap
  TENANT_HOST_PATTERN: '^(.*)-approuter.cfapps.sap.hana.ondemand.com'
  destinations: >
  [
    {"name":"srv-dest", "url":"https://???.cfapps.sap.hana.ondemand.com", "forwardAuthToken":true}
  ] 
```
4. For UI layer, standard UI5 development is followed.
   
   Once the UI code change is finished, you can start the web instance by selecting the `web` folder and click <kbd>Run</kbd>. Once the web instance is started, the UI is started automatically in a new browser window. 
