package com.qa.pages;

import com.qa.ultils.TestUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class ProductDetailsPage extends MenuPage {
    TestUtils utils = new TestUtils();
    @AndroidFindBy(xpath = "//android.widget.TextView[@text=\"Sauce Labs Backpack\"]") private WebElement SLBTitle;
    @AndroidFindBy (xpath = "//android.widget.TextView[@text=\"carry.allTheThings() with the sleek, streamlined Sly Pack that melds uncompromising style with unequaled laptop and tablet protection.\"]") private WebElement SLBTxt;
    @AndroidFindBy (accessibility = "test-BACK TO PRODUCTS") private WebElement backToProductsBtn;

//    @AndroidFindBy (accessibility = "test-Price" ) private WebElement SLBPrice;
    @AndroidFindBy  (xpath = "//android.widget.TextView[@text=\"Terms of Service | Privacy Policy\"]") private WebElement SLBPrivacy;

    public ProductDetailsPage(){
        PageFactory.initElements(new AppiumFieldDecorator(getDriver()), this);
    }

    public String getSLBTitle() {
        String title = getText(SLBTitle);
        utils.log("title is - " + title);
        return title;
    }

    public String getSLBTxt() {
        String txt = getText(SLBTxt);
        utils.log("txt is - " + txt);
        return txt;
    }

//    public String getSLBPrice() {
//        String price = getText(SLBPrice);
//        System.out.println("price is - " + price);
//        return price;
//    }

    public String getSLBPrivacy()
    {
        String privacy = getText(SLBPrivacy);
        utils.log("privacy is - " + privacy);
        return privacy;
    }

    public String scrollToSLBPriceAndGetSLBPrice(){
        return getText(scrollToElement());
    }

    public String scrollToSLBPrivacy()
    {
        return getText(scrollToElement());
    }

    public ProductsPage pressBackToProductsBtn() {
        utils.log("navigate back to products page");
        click(backToProductsBtn);
        return new ProductsPage();
    }

}
