PoC for **oDataV2** based on CAP

## Application Programming Model
The [SCP application programming model](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/00823f91779d4d42aa29a498e0535cdf.html) is followed.

* UI5 is used in the user interface layer
* Java is used in the service layer. [SAP Cloud SDK](https://mvnrepository.com/artifact/com.sap.cloud.servicesdk.prov/projects-parent)  is utilized to provide oData V4 service.
* HANA DB and HANA Core Data Service (CDS) is used in the data layer
     <img src="https://github.wdf.sap.corp/RC/Pictures/blob/master/rc/SCP%20Program%20Model.jpg" border="1"/>

## Development Guideline

1. [WebIDE Full-Stack Fiori](https://webidecp-fiori.dispatcher.int.sap.hana.ondemand.com/) is the default development IDE. Refer to [Develop Full-Stack To-Do app in WebIDE](https://blogs.sap.com/2017/10/23/sap-web-ide-ninja-4-develop-full-stack-to-do-app-in-sap-web-ide-part-1-database/) as examples to use WebIDE.
   
2. [Setup the WebIDE](https://wiki.wdf.sap.corp/wiki/display/CXSDEV/101001+Tips+with+HANA+Development) and connect to the development space.

    **Note** that each space need a Builder, in case the Builder is out-of-date you need go to Project Settngs -> Cloud Foundry in the WebIDE and reinstall it.`
    
3. Colne [current repository](https://github.wdf.sap.corp/RC/rc.git) to the WebIDE. Since the Github is not connected to Gerrit (code review tool), it's important not to add configuration to Gerrit.
   
4. [HANA CDS](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/855e00bd559742a3b8276fbed4af1008.html) is used to model database artifacts and [HDI (HANA Deployment Infrastructure)](https://wiki.wdf.sap.corp/wiki/display/CXSDEV/1010+HANA+Development) is used to manage the database design-time artifacts and the corresponding deployed run-time (catalog) objects. The data model can be edited in **db/src/data_model.hdbcds**, you can then activate and view the database artifacts in the WebIDE Full-Stack. Read [Wiki - HANA Development](https://wiki.wdf.sap.corp/wiki/display/CXSDEV/1010+HANA+Development) for more details.

   <img src="https://github.wdf.sap.corp/RC/Pictures/blob/master/rc/Build%20db.jpg" border="1" alt="Deploy DB artifacts"/>
 
   **Note** that the guidelines contained in data_model.hdbcds need to be followed.
  
5. For service layer, several Java technologys and frameworks are used. For detailed guide, refer to [Wiki - Cloud Development](https://wiki.wdf.sap.corp/wiki/display/CXSDEV/1+Cloud+Development).
   
   To provide **oData service**, srv/src/main/resources/edmx/**RCService.xml** need to be edited according to HANA data model changed in the previous step. Each oData entity should have one service implementation class in package com.sap.rc.main.service. More notes can be found in BaseService.java file.
   
   Once the service layer code change is finished, you can start the Java service instance by selecting the `srv` folder and click <kbd>Run</kbd>. Once the application is started, you can open the instance through the service URL in the WebIDE.
   <img src="https://github.wdf.sap.corp/RC/Pictures/blob/master/rc/Start%20Java%20service.jpg" border="1"/>

   **Note** that the service URL is generated based on space and user so that each developers can deploy independent Java instances at the same time to prevent unnecessary mutual interference. However the service instance is referred by the UI layer via application router, it's necessary to change the approuter **srv-dest** URL properties in mta.yaml to map to your specific service URL. **Do not** commit the mta.yaml file if you change it only for this purpose.
```
properties:
  XSAPPNAME: readinesscheck
  TENANT_HOST_PATTERN: '^(.*)-approuter.cfapps.sap.hana.ondemand.com'
  destinations: >
  [
    {"name":"srv-dest", "url":"https://???-rc-srv.cfapps.sap.hana.ondemand.com", "forwardAuthToken":true}
  ] 
```
6. For UI layer, standard UI5 development is followed.
   
   Once the UI code change is finished, you can start the web instance by selecting the `web` folder and click <kbd>Run</kbd>. Once the web instance is started, the UI is started automatically in a new browser window. 
   
   <img src="https://github.wdf.sap.corp/RC/Pictures/blob/master/rc/Start%20Web%20application.jpg" border="1"/>   

## MTA development with Eclipse
WebIDE is sufficient for HANA DB and UI development, however it also has some drawbacks: 
1. You always have to start the Java and the UI instance separately and starting the Java instance is relatively slow.
2. The development is stuck when there is issue with the IdP and SCP ( not always stable ) used by development space.
3. Much powerful capability with Java programming.

It’s possible to do Java and UI development with local Eclipse IDE and merge them locally as a single application by Maven.
