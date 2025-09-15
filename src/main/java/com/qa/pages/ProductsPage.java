
package com.qa.pages;

import com.qa.BaseTest;
import com.qa.ultils.TestUtils;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import javax.rmi.ssl.SslRMIClientSocketFactory;

public class ProductsPage extends MenuPage {

    TestUtils utils = new TestUtils();

    public ProductsPage() {
        PageFactory.initElements(new AppiumFieldDecorator(getDriver()), this);
    }

    @AndroidFindBy (xpath = "//android.widget.TextView[@text=\"PRODUCTS\"]")
    @iOSXCUITFindBy (xpath = "//")
    private WebElement productTitleTxt;

    @AndroidFindBy (xpath = "//android.widget.TextView[@content-desc=\"test-Item title\" and @text=\"Sauce Labs Backpack\"]") private WebElement SLBTitle;
    @AndroidFindBy (xpath = "//android.widget.TextView[@content-desc=\"test-Price\" and @text=\"$29.99\"]") private WebElement SLBPrice;

    // test scroll with different element
    @AndroidFindBy (xpath = "//android.widget.TextView[@text=\"Terms of Service | Privacy Policy\"]") private WebElement SLBPrivacy;
    
    public String getTitle() {
        String title = getText(productTitleTxt);
        utils.log("product page title is - " + title);
        return title;
    }

    public String getSLBTitle() {
        String title = getText(SLBTitle);
        utils.log("title is - " + title);
        return title;
    }

    public String getSLBPrice() {
        String price = getText(SLBPrice);
        utils.log("price is - " + price);
        return price;
    }

    public String getSLBPrivacy() {
        String privacy = getText(SLBPrivacy);
        utils.log("privacy is - " + privacy);
        return privacy;
    }

    public ProductDetailsPage pressSBTitle(){
        utils.log("press SB title link -");
        click(SLBTitle);
        return new ProductDetailsPage();
    }

}

