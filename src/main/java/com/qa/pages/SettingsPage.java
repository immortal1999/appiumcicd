package com.qa.pages;

import com.qa.BaseTest;
import com.qa.ultils.TestUtils;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.annotations.Test;

public class SettingsPage extends BaseTest {
    TestUtils utils = new TestUtils();
    @AndroidFindBy (accessibility = "test-LOGOUT") private WebElement logoutBtn;

    public SettingsPage() {
        PageFactory.initElements(new AppiumFieldDecorator(getDriver()), this);
    }

    public LoginPage pressLogOutBtn(){
        utils.log("press LogOut button");
        click(logoutBtn);
        return new LoginPage();
    }
}
