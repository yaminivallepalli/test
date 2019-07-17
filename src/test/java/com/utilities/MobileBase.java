package com.utilities;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.configurations.Constants;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import com.restassured.services.ReportPaths;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.touch.offset.PointOption;

public class MobileBase {
	
	public static String projectPath = System.getProperty("user.dir");
	public static String mobileReportsPath = projectPath + File.separator + "MobileReports" + File.separator + ReportPaths.reportPathName;
	private String nodeJSPath = System.getenv("NODE_PATH");
	private String appiumServerJSPath = System.getenv("APPIUM_JS_PATH");
	private AppiumDriverLocalService appiumDriverService;
	public AppiumDriver<MobileElement> appiumDriver;
	private ConfigFilesUtility configFileObj;
	
	/**************************************************** Desired Capabilities (Android/iOS) ************************************************/
	/*
	 * Set up desired capabilities and pass the Android app-activity and app-package to Appium
	 * Create AppiumDriver instance and connect to the Appium server
	 * It will launch the Application in Android Device using the configurations specified in Desired Capabilities
	 */

	public AppiumDriver<MobileElement> launchMobileDriver(ExtentReports reports) throws Exception {
		configFileObj = new ConfigFilesUtility();
		configFileObj.loadPropertyFile("DeviceCapabilities.properties");
		appiumDriverService = setUpAppiumDriver();
		startAppiumServer();
		
		DesiredCapabilities capabilities = new DesiredCapabilities();
		
		
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, configFileObj.getProperty("deviceName").trim());		
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, configFileObj.getProperty("platformName").trim());		
		capabilities.setCapability(MobileCapabilityType.FULL_RESET,  configFileObj.getProperty("fullreset").trim());	     
		capabilities.setCapability(MobileCapabilityType.UDID, configFileObj.getProperty("udid").trim());
		capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60*10); //ending the session 10 minutes
		capabilities.setCapability(MobileCapabilityType.APP, projectPath + File.separator + "mobile" + File.separator + (configFileObj.getProperty("app").trim().contains(".ipa") ? "ios" : "android") + File.separator +  configFileObj.getProperty("app").trim());
		
		if(configFileObj.getProperty("app").trim().contains(".ipa")) {
			capabilities.setCapability("bundleid", configFileObj.getProperty("appPackage").trim());
			capabilities.setCapability("wdaLaunchTimeout",60000); // launching webdriver agent time
			capabilities.setCapability("wdaStartupRetries", 2); //webdriveragent retries 4 times
			capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
			
			appiumDriver = new IOSDriver<MobileElement>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
		} else {
			capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS,true);
			capabilities.setCapability("autoAcceptAlerts", true);
			capabilities.setCapability("appPackage", configFileObj.getProperty("appPackage").trim());
			capabilities.setCapability("appActivity", configFileObj.getProperty("appActivity").trim());
			capabilities.setCapability("appWaitActivity", configFileObj.getProperty("appActivity").trim());
			//capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
			//capabilities.setCapability("unicodeKeyboard", "true");
			//capabilities.setCapability("resetKeyboard", "true");
			String platformVersion = configFileObj.getProperty("platformVersion");	
			if ((platformVersion != null) && !(platformVersion.trim().isEmpty())) {
				capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, configFileObj.getProperty("platformVersion").trim());
			} else {
				capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, configFileObj.getProperty("VERSION").trim());
			}
			if ((platformVersion != null) && !platformVersion.trim().isEmpty() && !(Integer.valueOf(platformVersion.trim().split("\\.")[0]) >= 7) ) {
				capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
			} else if (Double.valueOf(configFileObj.getProperty("VERSION").trim()) >= 7 ) {
				capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
			}
			appiumDriver = new AndroidDriver<MobileElement>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
		}
		//appiumDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		ExtentTest test = reports.startTest("Device Information");
		System.out.println("sessionDetails" + new JSONObject(appiumDriver.getSessionDetails()));
		JSONObject sessionObj = new JSONObject(appiumDriver.getSessionDetails());
		String deviceScreenSize = sessionObj.optString("deviceScreenSize");
		String pltformVersion = configFileObj.getProperty("platformVersion");
		String deviceModel = sessionObj.optString("deviceModel");
		String deviceManufacturer = sessionObj.optString("deviceManufacturer");
		String platformName = sessionObj.optString("platformName");
		
		if(configFileObj.getProperty("app").trim().contains(".ipa")) {
			JSONObject rectObj = sessionObj.optJSONObject("viewportRect");
			deviceScreenSize = "Width: " + rectObj.optString("width") + ", Height: " + rectObj.optString("height") + ", Top: " + rectObj.optString("top");
			deviceModel = sessionObj.optString("deviceName");
		} else {
			test.log(LogStatus.INFO, "Device Manufacture : " + deviceManufacturer);
		}
		test.log(LogStatus.INFO, "Device Model : " + deviceModel);
		//test.log(LogStatus.INFO, "Device Screen Size : " + deviceScreenSize);
		test.log(LogStatus.INFO, "Platform Name : " + platformName);
		test.log(LogStatus.INFO, "Platform Version : " + pltformVersion);
		
		return appiumDriver;
	}
	
	
	/********************************** Hide Keyboard (Android/iOS) ****************************/
	public void hideKeyboard() {
		if(appiumDriver != null) {
			if(configFileObj.getProperty("app").contains(".ipa")) {
				hideKeyBoard();
			} else {
				appiumDriver.hideKeyboard();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void hideKeyBoard() {
		IOSElement element = (IOSElement) appiumDriver.findElementByClassName("XCUIElementTypeKeyboard");
		Point keyboardPoint = element.getLocation();
		TouchAction touchAction = new TouchAction(appiumDriver);
		touchAction.tap(PointOption.point(keyboardPoint.getX() + 2, keyboardPoint.getY() - 2)).perform();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/************************************** APPIUM SETUP ***************************************/
	// Appium server setup service 
	private AppiumDriverLocalService setUpAppiumDriver() {
		 // Only for iOS/Mac
		if(configFileObj.getProperty("app").contains(".ipa") || isMac()) {
			nodeJSPath = configFileObj.getProperty("nodePath").trim();
			appiumServerJSPath = configFileObj.getProperty("appiumJSPATH").trim();
		}
		
		appiumDriverService = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
				.usingDriverExecutable(new File(nodeJSPath))
				.withAppiumJS(new File(appiumServerJSPath))
				.withIPAddress("127.0.0.1")
				.usingPort(4723));
		return appiumDriverService;
	}
	
	public boolean checkIfServerIsRunnning(int port) {
		
		boolean isServerRunning = false;
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.close();
		} catch (IOException e) {
			//If control comes here, then it means the port is in use
			isServerRunning = true;
		} finally {
			serverSocket = null;
		}
		return isServerRunning;
	}	
 
	
	// used to start appium server
	private boolean startAppiumServer() {
		if (appiumDriverService != null) {
			appiumDriverService.start();
			return appiumDriverService.isRunning();
		}
		return false;
	}
	
	// used to stop appium server
	protected void stopAppiumServer() {
		if (appiumDriverService != null) {
			appiumDriverService.stop();
			System.out.println(" Stopped Appium Server");
		}   
	}
	
	// add screen count for Report
	public void addScreensExecutionCount(){
		Constants.TOTAL_TC = Constants.TOTAL_TC + 1;
		Constants.IS_TESTCASE = true; Constants.iS_WEB = true;
	}
	
	
	/**************************************************** For iOS **************************************************/
	
	protected MobileElement findElement(String xpath) {
		MobileElement element = null;
		try {
			try {
				element = appiumDriver.findElementByXPath(xpath);
			} catch (Exception e) {
				waitForExpectedElement(appiumDriver, xpath, 60);
				element = appiumDriver.findElementByXPath(xpath);
			}
			if (element.isDisplayed()) {
				return element;
			} else {
				for (int i = 0; i < 5; i++) {
					scrollDown();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return element;
	}
	
	// It's not working latest appium versions
	public void scrolliniOS(String xpath) {
		MobileElement elements = appiumDriver.findElementByXPath(xpath);
		for (int second = 0;; second++) {
			if (second >= 25){
				break;
			}
			JavascriptExecutor js = (JavascriptExecutor) appiumDriver;

			HashMap<String, Object> scrollObjects = new HashMap<String,Object>();
			scrollObjects.put("direction", "down");
			scrollObjects.put("element", elements);
			js.executeScript("mobile: scroll", scrollObjects);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("scroll wont work for you");

			}
		}
	}
	
	/**************************************************** For ANDROID **********************************************/
	
	/************************************ Find element by Text **********************************************/
	
	/**
	 * used to find element by text  
	 * Less than android version 7 - Used UiScrollable for scrolling
	 * Greater than or equalTo android version 7 - Used TouchAction for scrolling to find the element
	 * @param xpath
	 * @return MobileElement
	 */
	// used to finding the element in screen automatically scrolls full page for finding the element by Text
	protected MobileElement findElementByText(String xpath) {
		
		MobileElement element = null;
		int maxSwipes =  10;
		String text = xpath.split("'")[1]; //Extracting the text from xpath
		try {		
			if (Double.valueOf(getVersion()) >= 7) {
				for (int i = 0;  i < maxSwipes; i++) {
					if(isElementFound(xpath)) {
					  element = appiumDriver.findElement(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector()."
								+ "scrollable(true).instance(0))"
								+ ".scrollIntoView(new UiSelector().text(\"" + text + "\").instance(0));"));
						break;
					} else {
						scrollDown();
					}
				}
			} else {
				try {
					if (new WebDriverWait(appiumDriver, 15).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).isDisplayed()) {
						element = appiumDriver.findElementByXPath(xpath);
					}
				} catch (Exception e) {
					element = scrollByText(text, xpath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return element;
	}
	

	private String getVersion() {
		JSONObject sessionObj = null;
		if(sessionObj == null) {
			sessionObj = new JSONObject(appiumDriver.getSessionDetails());
		}
		String version = sessionObj.optString("platformVersion").trim().split("\\.")[0];
		return version;
	}
	
	public boolean isElementFound(String text) {
        try {
           appiumDriver.findElement(By.xpath(text));
        } catch(Exception e){
            return false;
        }
        return true;
    }
	
	
	public MobileElement scrollByText(String text, String xpath) {
		try {
			return appiumDriver.findElement(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector().text(\"" + text + "\").instance(0));"));
		} catch (Exception e) {
			WebElement isElementPresence = waitForExpectedElement(appiumDriver, xpath, 60);
			if (isElementPresence.isDisplayed()) {
				return appiumDriver.findElementByXPath(xpath);
			}
		}
		return null;
	}
	
	/*************************************** Find element by Id *************************************/
	// used to finding the element in screen 
	// scrolls full page for finding the element by ID 
	protected MobileElement findElementById(String id) {
		MobileElement element = null;
		String xpath = id.split("'")[1];
		try {
			//element = (MobileElement) appiumDriver.findElementByAccessibilityId(xpath);
			element = appiumDriver.findElement(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
	            ".scrollIntoView(new UiSelector().resourceId(\"" + xpath + "\").instance(0))"));
		} catch (Exception e) {
			WebElement isElementPresence = waitForExpectedElement(appiumDriver, id, 60);
			if(isElementPresence.isDisplayed()) {
			element = appiumDriver.findElement(MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
	            ".scrollIntoView(new UiSelector().resourceId(\"" + xpath + "\").instance(0))"));
			}
		}
		System.out.println("Search Box Name - " + element.getText());
		return element;
	}
	
	//Only For android password field
	protected WebElement findElementByPwd(String Pwd, String value) {
		WebElement PasswordTextInputText = waitForExpectedElement(appiumDriver, Pwd, 60);
		PasswordTextInputText.sendKeys(value);
		return PasswordTextInputText;
	}
			
	// wait 60 seconds for finding the element
	public static WebElement waitForExpectedElement(AppiumDriver<MobileElement> appiumDriver, String locator, long timeOutInSeconds) {
		WebDriverWait wait = new WebDriverWait(appiumDriver, timeOutInSeconds);
		appiumDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
	}
	
	
	
	/*********************************** ScrollDown (Android/iOS) ***********************************/
	
	private void scrollDown() {
	    //if pressX was zero it didn't work for me
	    int pressX = appiumDriver.manage().window().getSize().width / 2;
	    // 4/5 of the screen as the bottom finger-press point
	    int bottomY = appiumDriver.manage().window().getSize().height * 4/5;
	    // just non zero point, as it didn't scroll to zero normally
	    int topY = appiumDriver.manage().window().getSize().height / 8;
	    //scroll with TouchAction by itself
	    scroll(pressX, bottomY, pressX, topY);
	}
	
	
	/*
	 * Don't forget that it's "natural scroll" where 
	 * fromY is the point where you press and toY where you release it
	 */
	@SuppressWarnings("rawtypes")
	private void scroll(int fromX, int fromY, int toX, int toY) {
	    TouchAction touchAction = new TouchAction(appiumDriver);
	    touchAction.longPress(PointOption.point(fromX, fromY)).moveTo(PointOption.point(toX, toY)).release().perform();
	}

	/************************************** Reports ******************************************/
	
	// These methods used to write the extent reports and logger
	public void testPass(String message, ExtentTest test, Logger logger) {
		test.log(LogStatus.PASS, message);
		logger.info(message);
	}

	public void testInfo(String message, ExtentTest test, Logger logger) {
		test.log(LogStatus.INFO, message);
		logger.info(message);
	}

	public void testFail(String message, ExtentTest test, Logger logger, AppiumDriver<MobileElement> mobileDriver) {
		test.log(LogStatus.FAIL, message);
		logger.error(message);
		Utilities.setMobilePlatform();
		test.log(LogStatus.INFO, "Screenshot Taken : " + Utilities.captureScreenshot(mobileDriver, message + " is Failed"));
	}

	// ScreenName Header for Reports
	public static void testLogHeader(ExtentTest test ,String data) {
		test.log(LogStatus.INFO, "<b style = 'background-color: #ffffff; color : #ff8f00 ; font-size : 18px' >"+ data + "</b>");
	}
	
	
	/*********************************** Find the Client OS ****************************************/
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	private static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}
	
	
	
	
//	@SuppressWarnings("rawtypes")
//	public void swipingHorizontal(String xpath) throws InterruptedException {
//		  //Get the size of screen.
//		  int size = 0;
//
//		  //Find swipe x points from screen's with and height.
//		  //Find x1 point which is at right side of screen.
//		  int x1 = (int) (appiumDriver.manage().window().getSize().width * 0.70);
//		  //Find x2 point which is at left side of screen.
//		  int x2 = (int) (appiumDriver.manage().window().getSize().width * 0.30);
//		  
//		  //Create object of TouchAction class.
//		  TouchAction action = new TouchAction(appiumDriver);
//		  
//		  //Find element to swipe from right to left.
//		 // MobileElement ele1 =  appiumDriver.findElementById(xpath);  
//		  //Create swipe action chain and perform horizontal(right to left) swipe.
//		  //Here swipe to point x1 Is at left side of screen. So It will swipe element from right to left.
//		 // action.longPress(LongPressOptions.longPressOptions().withElement(ElementOption.element(ele1))).moveTo(PointOption.point(x1,580)).release().perform();
// 
//		  //Find element to swipe from left to right.
//		  MobileElement ele2 =  appiumDriver.findElementById(xpath);
//		  //Create swipe action chain and perform horizontal(left to right) swipe.
//		  //Here swipe to point x2 Is at right side of screen. So It will swipe element from left to right.
//		  
//		  action.longPress(LongPressOptions.longPressOptions().withElement(ElementOption.element(ele2))).moveTo(PointOption.point(x2,580)).release().perform();
//	}
//	
//	public  void swipeLeft(String xpath) {
//		Point[] points = getXYtoHSwipe();
//		 MobileElement ele2 =  appiumDriver.findElementByXPath(xpath);
//		 int x =  ele2.getLocation().getX();
//		 int y =  ele2.getLocation().getY();
//
//		new TouchAction(appiumDriver).longPress(LongPressOptions.longPressOptions().withPosition(PointOption.point(x,y))).moveTo(PointOption.point(points[1].x, points[1].y)).release().perform();
//	}
//	
//	/**
//	 *
//	 * @return start and end points for horizontal(left-right) swipe
//	 */
//	private  Point[] getXYtoHSwipe() {
//		// Get screen size.
//		Dimension size = appiumDriver.manage().window().getSize();
//
//		// Find starting point x which is at right side of screen.
//		int startx = (int) (size.width * 0.70);
//		// Find ending point x which is at left side of screen.
//		int endx = (int) (size.width * 0.30);
//		// Find y which is in middle of screen height.
//		int startEndy = size.height / 2;
//
//		return new Point[] { new Point(startx, startEndy), new Point(endx, startEndy) };
//	}
//	
//
//	/**
//	 * Swipe from Left to Right
//	 */
//	public  void swipeRight(String xpath) {
//		Point[] points = getXYtoHSwipe();
//		 MobileElement ele2 =  appiumDriver.findElementByXPath(xpath);
//		 int x =  (int)(ele2.getLocation().getX() * 0.20);
//		 int y =  (int)(ele2.getLocation().getY() * 0.80);
//		new TouchAction(appiumDriver).longPress(LongPressOptions.longPressOptions().withPosition(PointOption.point(x,y))).moveTo(PointOption.point(points[0].x, points[0].y)).release().perform();
//	}
//	
	
}
