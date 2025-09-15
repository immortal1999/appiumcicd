package com.qa;

import com.qa.ultils.TestUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.screenrecording.CanRecordScreen;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BaseTest {
    protected static ThreadLocal <AppiumDriver> driver = new ThreadLocal<AppiumDriver>();
    protected static ThreadLocal <Properties> props = new ThreadLocal<Properties>();
    protected static ThreadLocal<HashMap<String, String>> strings = new ThreadLocal<HashMap<String, String>>();
    protected static ThreadLocal <String> platform = new ThreadLocal<String>();
    protected static ThreadLocal <String> dateTime = new ThreadLocal<String>();
    protected static ThreadLocal <String> deviceName = new ThreadLocal<String>();
    private static AppiumDriverLocalService server;
//    static Logger log = LogManager.getLogger(BaseTest.class.getName());


    TestUtils utils;
    // initialize this to every Test Page so it wont be the null value
    //        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    public void setDriver(AppiumDriver driver2){
        driver.set(driver2);
    }

    public Properties getProps(){
        return props.get();
    }

    public void setProps(Properties props2){
        props.set(props2);
    }

    public HashMap<String,String> getStrings(){
        return strings.get();
    }

    public void setStrings(HashMap<String,String> strings2){
        strings.set(strings2);
    }

    public String getPlatform(){
        return platform.get();
    }

    public void setPlatform(String platform2){
        platform.set(platform2);
    }

    public String getDateTime(){
        return dateTime.get();
    }

    public void setDateTime(String dateTime2){
        dateTime.set(dateTime2);
    }

    public String getDeviceName(){
        return deviceName.get();
    }

    public void setDeviceName(String deviceName2){
        deviceName.set(deviceName2);
    }



    @BeforeMethod
    public void beforeMethod() {
        System.out.println("super before method");
        String media = ((CanRecordScreen) getDriver()).startRecordingScreen();
    }

    @AfterMethod
    public synchronized void afterMethod(ITestResult result) throws FileNotFoundException {
        System.out.println("super after method");
        String media = ((CanRecordScreen) getDriver()).stopRecordingScreen();

        // if(result.getStatus() == 2) capture only on fail
            Map<String, String> params = result.getTestContext().getCurrentXmlTest().getAllParameters();
            String dir = "videos" + File.separator + params.get("platformName") + params.get("platformVersion") + params.get("deviceName") + File.separator + getDateTime() + File.separator + result.getTestClass().getRealClass().getSimpleName();

            File videoDir = new File(dir);

            synchronized (videoDir) {
                if (!videoDir.exists()) {
                    videoDir.mkdirs();
                }
            }
            try {
                FileOutputStream stream = new FileOutputStream(videoDir + File.separator + result.getName() + ".mp4" );
                stream.write(Base64.decodeBase64(media));
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


    }

    @BeforeSuite
    public void beforeSuite() {
        server = getAppiumServerDefault();
        server.start();
    }

    @AfterSuite
    public void afterSuite() {
        server.stop();
        System.out.println("Appium server stopped");
    }

    public AppiumDriverLocalService getAppiumServerDefault(){
        return AppiumDriverLocalService.buildDefaultService();
    }
    @Parameters({"emulator","platformName", "platformVersion", "deviceName","udid", "systemPort","chromeDriverPort"})
    @BeforeTest
    public void beforeTest(@Optional("androidOnly") String emulator,String platformName, String platformVersion, String deviceName,String udid,
                           @Optional("androidOnly") String systemPort, @Optional("androidOnly") String chromeDriverPort) throws Exception {


//        log.info("This is info message");
//        log.error("This is error message");
//        log.debug("This is debug message");
//        log.warn("This is warning message");

        utils = new TestUtils();
        setDateTime(utils.dateTime());
        setPlatform(platformName);
        setDeviceName(deviceName);

        InputStream inputStream = null;
        InputStream stringis = null;
        Properties props = new Properties();
        AppiumDriver driver = null;
        try{
            props = new Properties();
            String propFileName = "config.properties";
            String xmlFileName = "strings/strings.xml";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            props.load(inputStream);
            setProps(props);

            stringis = getClass().getClassLoader().getResourceAsStream(xmlFileName);
            utils = new TestUtils();
            setStrings(utils.parseStringXML(stringis));

            URI uri = URI.create(props.getProperty("appiumURL") + "4723" );

//            appium server -p 4724 to run multiple servers
            switch(platformName){
                case "Android":
                    UiAutomator2Options options = new UiAutomator2Options().
                            setPlatformName("Android").
                            setNewCommandTimeout(Duration.ofMinutes(5));
//                    String androidAppUrl = getClass().getResource(props.getProperty("androidAppLocation")).getFile();
//                    System.out.println(androidAppUrl);
                    if(emulator.equalsIgnoreCase("true")){
                        options.setAvd(deviceName);
                    } else{
                        options.setUdid(udid);
                    }

                    // install app
//                    options.setApp("C:\\Users\\nguyenv\\IdeaProjects\\TDDFrameWork\\src\\test\\resources\\app\\Android.SauceLabs.Mobile.Sample.app.2.7.1.apk");
                    options.setAutomationName("UiAutomator2").
                            setUdid("udid").
                            setAppPackage(props.getProperty("androidAppPackage")).
                            setAppActivity(props.getProperty("androidAppActivity")).
                            setSystemPort(Integer.parseInt(systemPort)).
                            setChromedriverPort(Integer.parseInt(chromeDriverPort)).
                            setNoReset(true).
                            //setChromeOptions(chromeArgs)
                            setAutoGrantPermissions(true).
                            setEnforceAppInstall(true).
                            setAvdLaunchTimeout(Duration.ofMinutes(3)).setNoReset(true);

                    URL url = uri.toURL();
                    driver = new AndroidDriver(url,options);
                    // check if app installed and install it to pixel_8 device
                    break;
//                case "iOS":
//                    URI iOSUri = URI.create(props.getProperty("appiumURL"));
//                    URL iOSUrl = iOSUri.toURL();
//
//                    driver = new IOSDriver(IOSUrl, caps);
            }
            setDriver(driver);
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        } finally {
            if(inputStream != null){
                inputStream.close();
            }
            if(stringis != null){
                stringis.close();
            }
        }
    }

    public void waitForVisibility(WebElement e){
        WebDriverWait wait = new WebDriverWait(getDriver(), TestUtils.WAIT);
        wait.until(ExpectedConditions.visibilityOf(e));
    }

    public void click(WebElement e){
        waitForVisibility(e);
        e.click();
    }

    public void sendKeys(WebElement e, String txt){
        waitForVisibility(e);
        e.sendKeys(txt);
    }

    public String getAttribute(WebElement e, String attribute){
        waitForVisibility(e);
        return e.getAttribute(attribute);
    }

    public String getText(WebElement e){
        return switch (getPlatform()) {
            case "Android" -> getAttribute(e, "text");
            case "iOS" -> getAttribute(e, "label");
            default -> null;
        };
    }


    public void closeApp(){
        switch(getPlatform()){
            case "Android" -> ((InteractsWithApps) getDriver()).terminateApp(getProps().getProperty("androidAppPackage"));
            case "iOS" -> ((InteractsWithApps) getDriver()).terminateApp(getProps().getProperty("iOSBundleId"));
        }
    }

    public void launchApp(){
        switch(getPlatform()){
            case "Android" -> ((InteractsWithApps) getDriver()).activateApp(getProps().getProperty("androidAppPackage"));
            case "iOS" -> ((InteractsWithApps) getDriver()).activateApp(getProps().getProperty("iOSBundleId"));
        }
    }

    public WebElement scrollToElement(){
//        return driver.findElement(AppiumBy.androidUIAutomator("new UiScrollable(new UiSelector().description(\"test-Inventory item page\")).scrollIntoView("+"new UiSelector().text(\"$29.99\"));"));
//        return driver.findElement(AppiumBy.androidUIAutomator(
//                "new UiScrollable(new UiSelector().description(\"test-Inventory item page\"))" +
//                        ".scrollIntoView(new UiSelector().text(\"$29.99\"));"
//        ));
        return getDriver().findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().description(\"test-Inventory item page\"))" +
                         ".scrollIntoView(new UiSelector().text(\"Terms of Service | Privacy Policy\"));"
        ));

    }

    public static AppiumDriver getDriver() {
        return driver.get();
    }

    @AfterTest
    public void afterTest() {
        driver.get().quit();
    }

}
