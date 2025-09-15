package com.qa.pages;

import com.qa.BaseTest;
import com.qa.ultils.TestUtils;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.annotations.Test;

public class MenuPage extends BaseTest {
    TestUtils utils = new TestUtils();
    @AndroidFindBy (xpath = "//android.view.ViewGroup[@content-desc=\"test-Menu\"]/android.view.ViewGroup/android.widget.ImageView")
    private WebElement settingsBtn;

    public MenuPage() {
        PageFactory.initElements(getDriver(), this);
    }

    public SettingsPage pressSettingsBtn() throws InterruptedException {
        utils.log("press Settings button");
        Thread.sleep(5000);
        click(settingsBtn);
        return new SettingsPage();
    }

}
