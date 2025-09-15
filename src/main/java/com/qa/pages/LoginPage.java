
package com.qa.pages;

import com.qa.BaseTest;
import com.qa.ultils.TestUtils;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class LoginPage extends BaseTest {
    TestUtils utils = new TestUtils();
    public LoginPage() {
        PageFactory.initElements(new AppiumFieldDecorator(getDriver()), this);
    }

    @AndroidFindBy(accessibility = "test-Username")
    @iOSXCUITFindBy(id = "test-Username")
    private WebElement userNameTxtFld;

    @AndroidFindBy (accessibility = "test-Password")
    @iOSXCUITFindBy(id = "test-Password")
    private WebElement passwordTxtFld;

    @AndroidFindBy (accessibility = "test-LOGIN")
    @iOSXCUITFindBy(xpath = "/**/")
    private WebElement loginBtn;

//    @AndroidFindBy (xpath = "//android.widget.TextView[@text=\"Username and password do not match any user in this service.\"]") private WebElement getErrTxt;
    @AndroidFindBy (uiAutomator = "new UiSelector().text(\"Username and password do not match any user in this service.\")")
    @iOSXCUITFindBy(xpath = "//")
    private WebElement errTxt;

    public LoginPage enterUserName(String username) {
        sendKeys(userNameTxtFld, username);
        utils.log("login with " + username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        sendKeys(passwordTxtFld, password);
        utils.log("password is  " + password);
        return this;
    }

    public ProductsPage pressLoginButton() {
        utils.log("press login button");
        click(loginBtn);
        return new ProductsPage();
    }
    public ProductsPage login(String username, String password) {
        enterUserName(username);
        enterPassword(password);
        return pressLoginButton();
    }

    public String getErrText() {
        String err = getText(errTxt);
        utils.log("error text is - " + err);
        return err;
    }
}

