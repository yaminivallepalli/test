package com.configurations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.relevantcodes.extentreports.ExtentReports;

public class ExtentConfigurations {

	private static ExtentReports reports;
	//private static int testCaseLength = 0;
	public static int passedDataSets;
	public static int failedDataSets;

	public static ExtentReports getExtentInstance(String reportsPath, String projectPath, String reportName) {
		
		if (reports == null) {
			String environmentVariable = System.getProperty("user.dir");
			reports = new ExtentReports(reportsPath, false);
			String extentReportConfigFile = customizeTestngConfigFile(reportName.toUpperCase()); 
			try {
				writeFile(extentReportConfigFile, "extent-config", environmentVariable, ".xml");
			} catch (Exception e) {
				e.printStackTrace();
			}
			reports.loadConfig(new File(projectPath + File.separator + "extent-config.xml"));
		}
		
	/*	if(isTestCaseLength) {
			testCaseLength = testCaseLength + 1;
			System.out.println("length" + testCaseLength);
		}
		*/
		return reports;
	}
	
	/*public static int getTestSetCount1() {
		return testCaseLength;
	}*/
	
	public static String getDatasetResultCount() {	
		return passedDataSets +" PASS / " + failedDataSets + " FAIL";
	}
	
	
	public static String customizeTestngConfigFile(String reportName) {

		String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" 
				+ "<extentreports>\r\n" 
				+ "  <configuration>\r\n"
				+ "    <!-- report theme -->\r\n" 
				+ "    <!-- standard, dark -->\r\n"
				+ "    <theme>standard</theme>\r\n" 
				+ "\r\n" 
				+ "    <!-- document encoding -->\r\n"
				+ "    <!-- defaults to UTF-8 -->\r\n" 
				+ "    <encoding>UTF-8</encoding>\r\n" + "\r\n"
				+ "    <!-- protocol for script and stylesheets -->\r\n" 
				+ "    <!-- defaults to https -->\r\n"
				+ "    <protocol>https</protocol>\r\n" 
				+ "\r\n" 
				+ "    <!-- title of the document -->\r\n"
				+ "    <documentTitle>" + reportName + "</documentTitle>\r\n" 
				+ "\r\n"
				+ "    <!-- report name - displayed at top-nav -->\r\n"
				+ "    <reportName>DevRabbit - </reportName>\r\n" + "\r\n"
				+ "    <!-- report headline - displayed at top-nav, after reportHeadline -->\r\n"
				+ "    <reportHeadline>TEST AUTOMATION FRAMEWORK</reportHeadline>\r\n" + "\r\n"
				+ "    <!-- global date format override -->\r\n" 
				+ "    <!-- defaults to yyyy-MM-dd -->\r\n"
				+ "    <dateFormat>yyyy-MM-dd</dateFormat>\r\n" + "\r\n"
				+ "    <!-- global time format override -->\r\n" 
				+ "    <!-- defaults to HH:mm:ss -->\r\n"
				+ "    <timeFormat>HH:mm:ss</timeFormat>\r\n" + "\r\n" 
				+ "    <!-- custom javascript -->\r\n"
				+ "    <scripts>\r\n" + "      <![CDATA[\r\n" 
				+ "                $(document).ready(function() {\r\n"
				+ "                    $(\".logo-content\").hide();\r\n"
				+ "                    $(\".logo-container\").html(\"" + reportName + "\");\r\n" 
				+ "                });\r\n"
				+ "            ]]>\r\n" 
				+ "function clickImage(e){\r\n" + 
				"			\r\n" + 
				"		var imageic = e.src;\r\n" + 
				"		var imagealt = e.alt;\r\n" + 
				"		\r\n" + 
				"		swal({\r\n" + 
				"		  title: imagealt,\r\n" + 
				"		  imageUrl: imageic,\r\n" + 
				"		  imageWidth: 600,\r\n" + 
				"		  imageHeight: 280,\r\n" + 
				"		  imageAlt: 'Custom image',\r\n" + 
				"		  animation: false\r\n" + 
				"		})\r\n" + 
				"		}"
				+ "    </scripts>\r\n" + "\r\n" 
				+ "    <!-- custom styles -->\r\n"
				+ "    <styles>\r\n" 
				+ "      <![CDATA[\r\n" 
				+ "       .logo-content, nav{\r\n"
				+ "        background-color: #ff8f00;\r\n" 
				+ "       }    \r\n" + "                            \r\n"
				+ "      .nav-right li {\r\n" + "        border-left:0px;\r\n" 
				+ "        font-size: 18px;    \r\n"
				+ "      }\r\n" + "      \r\n" 
				+ " .logo-container {\r\n" 
				+ "			width: 300px;\r\n"
				+ "		}\r\n" 
				+ "	  nav, nav .nav-wrapper i, nav a.button-collapse, .logo a, nav label {\r\n"
				+ "		    padding-left: 300px;\r\n" 
				+ "		}\r\n" + "              "
				+ "      .nav-right li:last-child {\r\n" 
				+ "      Display:none;\r\n" 
				+ "      }\r\n"
				+ "            ]]>\r\n" 
				+ "    </styles>\r\n" 
				+ "  </configuration>\r\n" 
				+ "</extentreports>\r\n" + "";

		return str;
	}
	
	public static void writeFile(String str, String className, String filePath, String fileExtension) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + File.separator + className + fileExtension), "UTF-8"));
		try {
			out.write(str);
		} finally {
			out.close();
		}
	}


}
