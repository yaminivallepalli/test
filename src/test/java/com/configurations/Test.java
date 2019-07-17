package com.configurations;

import com.restassured.services.APIService;

public class Test {
	public static void main(String[] args) {
		
		
		 String primaryInfo  = "{\"module_description\":\"Laser Tag URL\\nhttp://veda-dev.internal.nio.io:\","
				 + "\"mobile_platform\":null,"
				 + "\"report_upload_url\":\"http://192.168.1.142:8030/TAF_Automation_DR/UploadReportFile\","
				 + "\"browser_type\":null,"
				 + "\"project_url\":\"https://maps.googleapis.com/maps\","
				 + "\"testcase_id\":199,"
				 + "\"project_name\":\"NIO\","
				 + "\"testset_name\":null,"
				 + "\"is_web\":false,"
				 + "\"project_id\":80,"
				 + "\"sub_module_id\":94,"
				 + "\"sub_module_description\":\"Sub module of laser tag\","
				 + "\"testcase_name\":\"TC_Tag_007\","
				 + "\"executed_timestamp\":1532068010728,"
				 + "\"is_execute\":true,"
				 + "\"file_name\":null,"
				 + "\"testset_id\":0,"
				 + "\"is_generate\":false,"
				 + "\"project_description\":\"NIO Project\","
				 + "\"module_id\":77,"
				 + "\"user_id\":7,"
				 + "\"raw_type_format\":\"application/xml\","
				 + "\"bundle_id\":null,"
				 + "\"sub_module_name\":\"Tag\","
				 + "\"device_os_version\":null,"
				 + "\"module_name\":\"Laser Tag\"}";
		 
		 String datasetHeader = "TC_Tag_007-DS1";

		 String datasetResources = "/api/place/textsearch/xml?location=-41.319282,174.818717&radius=1000&sensor=true&query=cafe&key=AIzaSyDHYIc482xi6azcmKl3LpirSPwoLovAcuc";
		 int requestType = 1;
		 int bodyType = -1;

		String urlParams  = "[]";

		String headers  = "[]";

		String body ="{}";


		APIService.callRequest(primaryInfo, urlParams, headers, requestType, bodyType, body, datasetHeader, datasetResources, null, null);
	}
}
