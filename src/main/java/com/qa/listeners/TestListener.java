package com.qa.listeners;

import com.qa.BaseTest;
import com.qa.ultils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TestListener implements ITestListener {

    TestUtils utils = new TestUtils();

    public void onTestFailure(ITestResult result) {
        if(result.getThrowable()!=null){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            result.getThrowable().printStackTrace(pw);
            utils.log(sw.toString());
        }

        BaseTest base = new BaseTest();
        File file = base.getDriver().getScreenshotAs(OutputType.FILE);

        Map<String, String> params = new HashMap<String, String>();
        params = result.getTestContext().getCurrentXmlTest().getAllParameters();

        String imagePath = "Screenshots" + File.separator + params.get("platformName") + "_" + params.get("platformVersion")
                + "_" + params.get("deviceName") +  File.separator + base.getDateTime() + File.separator
                + result.getTestClass().getRealClass().getSimpleName() + File.separator + result.getName() + ".png";
        String completeImagePath = System.getProperty("user.dir") + File.separator + imagePath;


        System.out.println("CompleteImgPath" + completeImagePath);
        try {
//            FileUtils.copyFile(file, new File(imagePath));
            Reporter.log("This is the sample screenshot");
            Reporter.log("<a href='" + completeImagePath +
                    "'> <img src='" + completeImagePath + "' height='400' width='400'/> </a>");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
