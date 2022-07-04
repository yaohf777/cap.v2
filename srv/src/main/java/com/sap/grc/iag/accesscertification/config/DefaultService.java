package com.sap.grc.iag.accesscertification.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class DefaultService {

	@GetMapping("/check")
    // http://localhost:8080/v2/check
    public String get() {
        return "Default controller works fine. oData service is provided through /v2/odata/." ;
    }

//  @BeforeRead(entity = "Orders", serviceName = "CatalogService")
//  public BeforeReadResponse beforeReadOrders(ReadRequest req, ExtensionHelper h) {
//      log.error("##### Orders - beforeReadOrders ########");
//      return BeforeReadResponse.setSuccess().response();
//  }
//
//  @AfterRead(entity = "Orders", serviceName = "CatalogService")
//  public ReadResponse afterReadOrders(ReadRequest req, ReadResponseAccessor res, ExtensionHelper h) {
//      EntityData ed = res.getEntityData();
//      EntityData ex = EntityData.getBuilder(ed).addElement("amount", 1000).buildEntityData("Orders");
//      return ReadResponse.setSuccess().setData(ex).response();
//  }
//
//  @AfterQuery(entity = "Orders", serviceName = "CatalogService")
//  public QueryResponse afterQueryOrders(QueryRequest req, QueryResponseAccessor res, ExtensionHelper h) {
//      List<EntityData> dataList = res.getEntityDataList(); // original list
//      List<EntityData> modifiedList = new ArrayList<EntityData>(dataList.size()); // modified list
//      for (EntityData ed : dataList) {
//          EntityData ex = EntityData.getBuilder(ed).addElement("amount", 1000).buildEntityData("Orders");
//          modifiedList.add(ex);
//      }
//      return QueryResponse.setSuccess().setData(modifiedList).response();
//  }
}