package com.qa.tests;

import com.qa.BaseTest;
import com.qa.pages.LoginPage;
import com.qa.pages.ProductsPage;
import com.qa.ultils.TestUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

public class LoginTests extends BaseTest {
    LoginPage loginPage;
    ProductsPage productsPage;
    JSONObject loginUsers;
    TestUtils utils = new TestUtils();
    @BeforeClass
    public void beforeClass() throws IOException {
        InputStream datais = null;
        try {
            String dataFileName = "data/loginUsers.json";
            datais = getClass().getClassLoader().getResourceAsStream(dataFileName);
            JSONTokener tokener = new JSONTokener(datais);
            loginUsers = new JSONObject(tokener);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (datais != null) {
                datais.close();
            }
        }
        closeApp();
        launchApp();
    }


    @AfterClass
    public void afterClass() {

    }

    @BeforeMethod
    public void beforeMethod(Method m) {
        utils.log("loginTest before method");
        loginPage = new LoginPage();
        utils.log("\n" + "***** starting test:" + m.getName() + "*****" + "\n");
    }

    @AfterMethod
    public void afterMethod() {
        utils.log("loginTest after method");
    }

    @Test
    public void invalidUserNameTest() {
        loginPage.enterUserName(loginUsers.getJSONObject("invalidUser").getString("username"));
        loginPage.enterPassword(loginUsers.getJSONObject("invalidUser").getString("password"));
        loginPage.pressLoginButton();

        String actualErrTxt = loginPage.getErrText();
        String expectedErrTxt = getStrings().get("err_invalid_username_or_password");
        utils.log("actual error txt - " + actualErrTxt + "\n" + "expected error txt - " + expectedErrTxt);

        Assert.assertEquals(actualErrTxt, expectedErrTxt);
    }
    @Test
    public void invalidPassword()
    {
        loginPage.enterUserName(loginUsers.getJSONObject("invalidPassword").getString("username"));
        loginPage.enterPassword(loginUsers.getJSONObject("invalidPassword").getString("password"));
        loginPage.pressLoginButton();

        String actualErrTxt = loginPage.getErrText() ;
        String expectedErrTxt = getStrings().get("err_invalid_username_or_password");
        utils.log("actual error txt - " + actualErrTxt + "\n" + "expected error txt - " + expectedErrTxt);

        Assert.assertEquals(actualErrTxt, expectedErrTxt);
    }

    @Test
    public void successfulLogin(){
        loginPage.enterUserName(loginUsers.getJSONObject("validUser").getString("username"));
        loginPage.enterPassword(loginUsers.getJSONObject("validUser").getString("password"));
        productsPage = loginPage.pressLoginButton();

        String actualProductTitle = productsPage.getTitle();
        String expectedProductTitle = getStrings().get("product_title");
        utils.log("actual title - " + actualProductTitle + "\n" + "expected title - " + expectedProductTitle );

        Assert.assertEquals(actualProductTitle, expectedProductTitle);
    }

}
