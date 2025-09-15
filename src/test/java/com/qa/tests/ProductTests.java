package com.qa.tests;

import com.qa.BaseTest;
import com.qa.pages.LoginPage;
import com.qa.pages.ProductDetailsPage;
import com.qa.pages.ProductsPage;
import com.qa.pages.SettingsPage;
import com.qa.ultils.TestUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import ui.DeepLink;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;

public class ProductTests extends BaseTest {
    LoginPage loginPage;
    ProductsPage productsPage;

    JSONObject loginUsers;
    SettingsPage settingsPage;
    ProductDetailsPage productDetailsPage;
    TestUtils utils = new TestUtils();

    @BeforeClass
    public void beforeClass() throws IOException {
        InputStream datais=null;
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
        loginPage = new LoginPage();
        utils.log("\n" + "***** starting test:" + m.getName() + "*****" + "\n");

        productsPage = loginPage.login(loginUsers.getJSONObject("validUser").getString("username"),
                loginUsers.getJSONObject("validUser").getString("password"));
    }

    @AfterMethod
    public void afterMethod() {
        closeApp();
        launchApp();
    }

    @Test
    public void validateProductOnProductsPage() throws InterruptedException, SQLException {
        SoftAssert sa = new SoftAssert();

        DeepLink.OpenAppWith("swaglabs://swag-overview/0,1");
        ProductsPage productsPage = new ProductsPage();

        String SLBTitle = productsPage.getSLBTitle();
        sa.assertEquals(SLBTitle, getStrings().get("products_page_slb_title"));

        String SLBPrice = productsPage.getSLBPrice();
        sa.assertEquals(SLBPrice, getStrings().get("products_page_slb_price"));

//        settingsPage = productsPage.pressSettingsBtn();
        sa.assertAll();
    }

    @Test
    public void validateProductOnProductsDetailPage() throws InterruptedException, SQLException {
        SoftAssert sa = new SoftAssert();

        DeepLink.OpenAppWith("swaglabs://swag-overview/0,1");
        ProductsPage productsPage = new ProductsPage();

        productDetailsPage = productsPage.pressSBTitle();

        String SLBTitle = productDetailsPage.getSLBTitle();
        sa.assertEquals(SLBTitle, getStrings().get("product_details_page_slb_title"));

        String SLBTxt = productDetailsPage.getSLBTxt();
        sa.assertEquals(SLBTxt, getStrings().get("product_details_page_slb_txt"));

//        String SLBPrice = productDetailsPage.scrollToSLBPriceAndGetSLBPrice();
//        sa.assertEquals(SLBPrice, strings.get("product_details_page_slb_price"));

        String SLBPrivacy = productDetailsPage.scrollToSLBPrivacy();
        sa.assertEquals(SLBPrivacy, getStrings().get("product_details_page_slb_privacy"));


        productsPage = productDetailsPage.pressBackToProductsBtn();
//        settingsPage = productsPage.pressSettingsBtn();

        sa.assertAll();
    }


}
