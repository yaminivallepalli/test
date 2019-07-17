package com.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import com.restassured.services.ReportPaths;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

public class BaseClass {
	public WebDriver driver;
	public static String projectPath = System.getProperty("user.dir");
	public static String reportsPath = projectPath + File.separator + "WebReports" + File.separator
			+ ReportPaths.reportPathName;
	public static String mobileReportsPath = projectPath + File.separator + "MobileReports" + File.separator
			+ ReportPaths.reportPathName;
	public String chromeDriverPath = projectPath + File.separator + "Resources" + File.separator + "chromedriver.exe";
	public String geckoFireFoxDriverPath = projectPath + File.separator + "Resources" + File.separator
			+ "geckodriver.exe";
	public String iEDriverPath = projectPath + File.separator + File.separator + "Resources" + File.separator
			+ "IEDriverServer.exe";
	public AppiumDriver<MobileElement> appiumDriver;

	// Explicit wait method
	public static WebElement waitForExpectedElement(WebDriver driver, final By locator, int time) {
		WebDriverWait wait = new WebDriverWait(driver, time);
		return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public static WebElement waitForExpectedElement(WebDriver driver, WebElement element) {
	
		initialInputDataClear(element); // if any text in input it will clear
		WebDriverWait wait = new WebDriverWait(driver, 20);
		return wait.until(ExpectedConditions.visibilityOf(element));
	}

	public static String initialInputDataClear(WebElement webElement) {
		String str = webElement.toString();
		try {
			if (str != null && str.contains("INPUT")) {
			String[] listString = null;
			if (str.contains("xpath")) {
				listString = str.split("xpath:");
			} else if (str.contains("id")) {
				listString = str.split("id:");
			}
			String last = listString[1].trim();
			String xpath = last.substring(0, last.length() - 1);
			if (xpath != null && xpath.contains("INPUT")) {
				webElement.clear();
			}
			}
		} catch (Exception e) {
			System.out.println("Not editable input");
		}
		return str;
	}

	// Explicit wait
	public static WebElement waitForExpectedElement(WebDriver driver, final By locator) {
		WebDriverWait wait = new WebDriverWait(driver, 120);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	// Explicit wait method
	public boolean objectExists(WebDriver driver, final By locator) {
		try {
			waitForPageToLoad();
			WebDriverWait wait = new WebDriverWait(driver, 30);
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// Method for Retry and It executes the failed test case based on our count
	public class Retry implements IRetryAnalyzer {
		private int count = 0;
		private static final int maxTry = 3;

		public boolean retry(ITestResult iTestResult) {
			if (!iTestResult.isSuccess()) { // Check if test not succeed
				if (count < maxTry) { // Check if maxtry count is reached
					count++; // Increase the maxTry count by 1
					iTestResult.setStatus(ITestResult.FAILURE); // Mark test as failed
					return true; // Tells TestNG to re-run the test
				} else {
					iTestResult.setStatus(ITestResult.FAILURE); // If maxCount reached,test marked as failed
				}
			} else {
				iTestResult.setStatus(ITestResult.SUCCESS); // If test passes, TestNG marks it as passed
			}
			return false;
		}
	}

	// Explicit wait method (While Script Execution we need to pass time limit)
	public boolean objectExists(WebDriver driver, final By locator, int timeout) {
		try {
			waitForPageToLoad();
			WebDriverWait wait = new WebDriverWait(driver, timeout);
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// Explicit wait method (While Script Execution we need to pass time limit
	public void waitForPageToLoad() {
		(new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return (((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return document.readyState")
						.equals("complete"));
			}
		});
	}

	@SuppressWarnings("deprecation")
	public WebDriver launchBrowser(String browserName, ConfigFilesUtility configFileObj) {
		if (!isWindows()) {
			if (isSolaris() || isUnix()) {
				chromeDriverPath = chromeDriverPath.replace(".exe", "");
				geckoFireFoxDriverPath = geckoFireFoxDriverPath.replace(".exe", "");
			} else if (isMac()) {
				chromeDriverPath = chromeDriverPath.replace("chromedriver.exe", "macChromeDriver");
				geckoFireFoxDriverPath = geckoFireFoxDriverPath.replace("geckodriver.exe", "macGeckodriver");
			}
		}

		if (browserName.equalsIgnoreCase("chrome")) {
			System.setProperty("webdriver.chrome.driver", chromeDriverPath);
			if (isSolaris() || isUnix()) {
				ChromeOptions options = new ChromeOptions();
				options.addArguments("start-maximized"); // open Browser in maximized mode
				options.addArguments("disable-infobars"); // disabling infobars
				options.addArguments("--disable-extensions"); // disabling extensions
				options.addArguments("--disable-gpu"); // applicable to windows os only
				options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
				options.addArguments("--no-sandbox"); // Bypass OS security model
				options.addArguments("--headless"); // this line makes run in linux environment with jenkins
				driver = new ChromeDriver(options);
			} else {
				driver = new ChromeDriver();
			}

			System.out.println("Chrome Browser is Launched");
		} else if (browserName.equalsIgnoreCase("mozilla")) {
			System.setProperty("webdriver.gecko.driver", geckoFireFoxDriverPath);

			driver = new FirefoxDriver();

			System.out.println("FireFox Browser is Launched");
		} else if (browserName.equalsIgnoreCase("safari")) {
			// Note : Should AllowRemoteAutomation in safari browser DeveloperMenu
			// Directions -- > launchSafariBrowser --> Preferences --> Advanced Tab -->
			// Show Developer Menu --> Click on DevloperMenu --> Enable
			// AllowRemoteAutomation
			// System.setProperty("webdriver.safari.noinstall", "true");
			driver = new SafariDriver();
			driver.get("http://www.google.com");
			System.out.println("FireFox Browser is Launched");
		} else if (browserName.equalsIgnoreCase("ie")) {
			if (!isWindows()) {
				System.out.println("IE Browser not supported for this OS.");
				return null;
			}
			DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			System.setProperty("webdriver.ie.driver", iEDriverPath);
			driver = new InternetExplorerDriver(capabilities);
			System.out.println("IE Browser is Launched");
		}

		// driver.manage().deleteAllCookies();
		driver.get(configFileObj.getProperty("URL"));
		// driver.manage().window().maximize();
		// driver.manage().window().fullscreen();
		Dimension d = new Dimension(1382, 744);
		// Resize the current window to the given dimension
		driver.manage().window().setSize(d);
		return driver;
	}

	private String OS = System.getProperty("os.name").toLowerCase();

	public boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

	public void testLogHeader(ExtentTest test, String data) {
		if (test != null)
			test.log(LogStatus.INFO,
					"<b style = 'background-color: #ffffff; color : #ff8f00 ; font-size : 18px' >" + data + "</b>");
	}

	public void printSuccessLogAndReport(ExtentTest test, Logger logger, String data) {
		if (test != null)
			test.log(LogStatus.PASS, data);
		if (logger != null)
			logger.info(data);
	}

	public void printFailureLogAndReport(ExtentTest test, Logger logger, String data) {
		if (test != null)
			test.log(LogStatus.FAIL, data);
		if (logger != null)
			logger.error(data);
		String name = "";
		if (data.toString().length() <= 20) {
			name = data.toString();
		} else {
			name = data.toString().substring(0, 10);
		}
		test.log(LogStatus.INFO, "Screenshot Taken : " + Utilities.captureScreenshot(driver, name));
	}

	public void printInfoLogAndReport(ExtentTest test, Logger logger, String data) {
		logger.info(data);
		test.log(LogStatus.INFO, data);
	}

	public void tearDown(ExtentReports reports, ExtentTest test) throws Exception {
		reports.endTest(test);
		reports.flush();
		driver.quit();
	}

	// mouseHover
	public void mouseHover(WebDriver webDriver, WebElement element) {

		Actions action = new Actions(webDriver);
		action.moveToElement(element).build().perform();
		// action.moveToElement(we).moveToElement(driver.findElement(By.xpath(elementClickXpath))).click().build().perform();
	}

	// window
	String parentHandle = "";

	public void windowHandle(WebDriver webDriver) {
		parentHandle = webDriver.getWindowHandle();
		Set<String> handles = webDriver.getWindowHandles();
		for (String windowHandles : handles) {
			System.out.println(windowHandles);
			webDriver.switchTo().window(windowHandles);
		}
	}

	public void switchToParentWindow(WebDriver webDriver) {
		if (parentHandle != null && !parentHandle.isEmpty()) {
			webDriver.switchTo().window(parentHandle);
		}
	}

	// upload a file
	public void uploadFile(String name, String xpath) {
		try {
			WebElement element = driver.findElement(By.xpath(xpath));
			waitForExpectedElement(driver, element);
			element.sendKeys(name);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	// Dropdown

	public void dropDownHandle(String xpath, String optionName) {
		Select oSelect = new Select(driver.findElement(By.xpath(xpath)));
		// List<WebElement> elementCount = oSelect.getOptions();
		// int iSize = elementCount.size();
		oSelect.selectByIndex(4);
		oSelect.selectByValue(optionName);
		// oSelect.selectByVisibleText("Europe");
	}

	PDFManager pdfManager;
	Document document;
	String fileName = ReportPaths.reportPathName.replace("html", "pdf");
	String downloadsFolder = System.getProperty("user.dir") + File.separator + "pdfFiles" + File.separator;

	public void validatePDF(WebDriver driver, ExtentReports reports, ExtentTest test) {

		test.log(LogStatus.INFO,
				"<b style = 'background-color: #ffffff; color : #ff8f00 ; font-size : 18px' ><a href=\"/Reports_TAFAutomation_DR/Reports/"
						+ ReportPaths.reportPathName.replace("html", "pdf")
						+ "\" target=\"_blank\">Input PDF file</a></b>");

		try {
			Map<String, String> dataMap = new HashMap<String, String>();
			pdfManager = new PDFManager();
			document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream("pdfFiles" + File.separator + fileName));
			document.open();
			WebElement departingElement = driver.findElement(By.xpath(
					"//HTML/BODY[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/FONT[1]/B[1]"));
			String departingItem = departingElement.getText();
			dataMap.put("Departing", departingItem);

			WebElement returnElement = driver.findElement(By.xpath(
					"//HTML/BODY[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/FONT[1]/B[1]"));
			String returningItem = returnElement.getText();
			dataMap.put("Returning", returningItem);

			Font bold = FontFactory.getFont(FontFactory.COURIER_BOLD, 20, Font.BOLD);
			document.add(new Paragraph("FLIGHT CONFIRMATION", bold));
			String confMessage = driver.findElement(By.xpath("//font[contains(text(),'itinerary has been booked')]"))
					.getText();
			document.add(new Paragraph(confMessage.trim()));
			// test.log(LogStatus.PASS, confMessage);
			printSuccessLogAndReport(test, null, confMessage);
			dataMap.put("Confirmation Message", confMessage.trim());

			String confNumber = driver
					.findElement(By.xpath("//font[contains(text(),'itinerary has been booked')]/following::b[1]/font"))
					.getText();
			document.add(new Paragraph(confNumber.trim()));
			// test.log(LogStatus.PASS, confNumber);
			printSuccessLogAndReport(test, null, confNumber);
			dataMap.put("Confirmation Number", confNumber.trim());

			String departingInfo = driver.findElement(By.xpath("//font[text()='Departing']")).getText();
			document.add(new Paragraph(departingInfo));

			String departingInfo1 = driver.findElement(By.xpath("//font[text()='Departing']/following::b[1]"))
					.getText();
			document.add(new Paragraph(departingInfo1));

			String returningInfo = driver.findElement(By.xpath("//font[text()='Returning']")).getText();
			document.add(new Paragraph(returningInfo));

			String returningInfo1 = driver.findElement(By.xpath("//font[text()='Returning']/following::b[1]"))
					.getText();
			document.add(new Paragraph(returningInfo1));

			String passengerInfo = driver.findElement(By.xpath("//b[text()='Passengers']")).getText();
			document.add(new Paragraph(passengerInfo));

			String passengerInfo1 = driver.findElement(By.xpath("//b[text()='Passengers']/following::font[1]"))
					.getText();
			document.add(new Paragraph(passengerInfo1.trim()));
			dataMap.put("No of Passengers", passengerInfo1.trim());

			String totalTaxes = driver.findElement(By.xpath("//font[contains(text(),'Taxes:')]/following::b[1]"))
					.getText();
			document.add(new Paragraph("Total Taxes: " + totalTaxes.trim()));
			dataMap.put("Total Taxes", totalTaxes.trim());

			String totalPrice = driver
					.findElement(By.xpath("//font[contains(text(),'including taxes')]/following::b[1]")).getText();
			document.add(new Paragraph("Total Price (including taxes): " + totalPrice.trim()));
			dataMap.put("Total Price", totalPrice.trim());

			// Closing Document
			document.close();
			// Closing Writer
			writer.close();

			// PDF File Validations with Application Data
			test.log(LogStatus.INFO, "PDF File Validations Started");

			String flightBookingData = getFlightData1();
			if (flightBookingData.contains(dataMap.get("Confirmation Message"))) {
				printSuccessLogAndReport(test, null, "Confirmation Message is Displayed in PDF as per Application as : "
						+ dataMap.get("Confirmation Message"));
				// test.log(LogStatus.PASS, "Confirmation Message is Displayed in PDF as per
				// Application as : " + dataMap.get("Confirmation Message"));
			} else {
				printFailureLogAndReport(test, null, "Confirmation Message is NOT Displayed in PDF as per Application");
				// test.log(LogStatus.FAIL, "Confirmation Message is NOT Displayed in PDF as per
				// Application");
			}

			if (flightBookingData.contains(dataMap.get("Confirmation Number"))) {
				printSuccessLogAndReport(test, null, "Confirmation Number is Displayed in PDF as per Application as : "
						+ dataMap.get("Confirmation Number"));
			} else {
				printFailureLogAndReport(test, null, "Confirmation Number is NOT Displayed in PDF as per Application");
			}

			if (dataMap.get("Departingg") != null && flightBookingData.contains(dataMap.get("Departing"))) {
				printSuccessLogAndReport(test, null,
						"Departing From is Displayed in PDF as per Application as : " + dataMap.get("Departing"));
			} else {
				printFailureLogAndReport(test, null, "Departing From is NOT Displayed in PDF as per Application");
			}

			if (dataMap.get("Returning") != null && flightBookingData.contains(dataMap.get("Returning"))) {
				printSuccessLogAndReport(test, null,
						"Returning is Displayed in PDF as per Application as : " + dataMap.get("Returning"));
			} else {
				printFailureLogAndReport(test, null, "Returning is NOT Displayed in PDF as per Application");
			}

			if (flightBookingData.contains(dataMap.get("No of Passengers"))) {
				printSuccessLogAndReport(test, null, "No of Passengers is Displayed in PDF as per Application as : "
						+ dataMap.get("No of Passengers"));
			} else {
				printFailureLogAndReport(test, null, "No of Passengers is NOT Displayed in PDF as per Application");
			}

			String flightBookingData2 = getFlightData2();

			if (flightBookingData2.contains(dataMap.get("Total Taxes"))) {
				printSuccessLogAndReport(test, null,
						"Total Taxes is Displayed in PDF as per Application as : " + dataMap.get("Total Taxes"));
			} else {
				printFailureLogAndReport(test, null, "Total Taxes is NOT Displayed in PDF as per Application");
			}

			String flightBookingData3 = getFlightData3();

			if (flightBookingData3.contains(dataMap.get("Total Price"))) {
				printSuccessLogAndReport(test, null,
						"Total Price is Displayed in PDF as per Application as : " + dataMap.get("Total Price"));
			} else {
				printFailureLogAndReport(test, null, "Total Price is NOT Displayed in PDF as per Application");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		reports.endTest(test);
		reports.flush();
		driver.quit();
	}

	public String[] getPDFData() throws Throwable {
		// System.out.println("File Path : "+downloadsFolder+fileName);
		// test.log(LogStatus.INFO, "File Path : "+downloadsFolder+fileName);
		pdfManager.setFilePath(downloadsFolder + fileName);
		String pdf = pdfManager.ToText();
		// System.out.println(pdf);
		String[] pdfInText = pdf.split(":");
		return pdfInText;
	}

	public String getFlightData1() {

		String[] data = null;
		try {
			data = getPDFData();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		String flightData = data[0].trim();
		return flightData;
	}

	public String getFlightData2() {
		String[] data = null;
		try {
			data = getPDFData();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		String flightData = data[1].trim();
		return flightData;
	}

	public String getFlightData3() {
		String[] data = null;
		try {
			data = getPDFData();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		String flightData = data[2].trim();
		return flightData;
	}

}
