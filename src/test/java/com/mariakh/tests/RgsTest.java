package com.mariakh.tests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RgsTest {
    private WebDriver driver;
    private WebDriverWait wait;

    // mvn test -Dtest=RgsTest -Dbrowser=edge
    // mvn test -Dtest=RgsTest -Dbrowser=firefox

    @BeforeEach
    public void before() {
        String browser = System.getProperty("browser", "chrome");
        switch (browser) {
            case "chrome":
                System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
                driver = new ChromeDriver();
                break;
            case "firefox":
                System.setProperty("webdriver.gecko.driver", "src/test/resources/geckodriver.exe");
                driver = new FirefoxDriver();
                break;
            case "edge":
                System.setProperty("webdriver.edge.driver", "src/test/resources/msedgedriver.exe");
                driver = new EdgeDriver();
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); //ожидание везде, где вызван findByElement
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10)); //проверяет загружена ли страница, прежде чем искать элемент
        driver.get("http://www.rgs.ru");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofSeconds(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Kotov Oleg Petrovich", "Ivanov Ivan Ivanovich", "Pesov Igor Petrovich"})
    public void test(String name) {

        //Провверить наличие всплывающего окна с подпиской и при необходимости закрыть его
        boolean isSwitchToFrame = isSwitchToFrame();

        closeFrameIfSwitched(isSwitchToFrame);

        //Закрыть окно с куки
        WebElement cookieAcceptButton = driver.findElement(By.xpath("//button[@class = 'btn--text']"));
        wait.until(ExpectedConditions.elementToBeClickable(cookieAcceptButton)).click();
        //cookieAcceptButton.click();

        //Кликнуть на раздел 'Компаниям'
        WebElement companyMenu = driver.findElement(By.xpath("//a[@href = '/for-companies']"));
        companyMenu.click();

        if (!isSwitchToFrame) {
            closeFrameIfSwitched(isSwitchToFrame());
        }

        //Подожать и проверить урл
        wait.until(ExpectedConditions.urlToBe("https://www.rgs.ru/for-companies"));
        //Assertions.assertEquals("https://www.rgs.ru/for-companies", driver.getCurrentUrl(), "Не перешли в раздел 'Компаниям'");
        Assertions.assertTrue(companyMenu.getAttribute("class").contains("active"));

        //Кликнуть на раздел 'Здоровье'
        WebElement healthMenu = driver.findElement(By.xpath("//span[@class = 'padding' and text() = 'Здоровье']"));
        healthMenu.click();

        //В выпадающем меню выбрать 'Добровольное медицинское страхование', проверить его видимость и кликнуть
        WebElement healthMenuDD = driver.findElement(By.xpath("//a[contains(@href, 'dobrovolnoe')]"));
        wait.until(ExpectedConditions.visibilityOf(healthMenuDD));
        healthMenuDD.click();

        //Найти заголовок 'Добровольное медицинское страхование' и проверить его наличие
        WebElement healthTitle = driver.findElement(By.xpath("//h1[contains(@class, 'title')]"));
        Assertions.assertEquals("Добровольное медицинское страхование"
                , healthTitle.getText()
                , "Не найден заголовок 'Добровольное медицинское страхование'"
        );

        //Найти кнопку 'Отправить заявку', проверить её кликабельность и кликнуть
        //WebElement sendRequest = driver.findElement(By.xpath("//button[@class = 'action-item btn--basic']"));
        WebElement sendRequest = driver.findElement(By.xpath("//a[@class = 'action-item btn--basic']"));
        wait.until(ExpectedConditions.elementToBeClickable(sendRequest));
        sendRequest.click();

        //Проверить наличие формы для заполнения
        WebElement formToFill = driver.findElement(By.xpath("//section[contains(@class, 'section-form-anchor')]"));
        Assertions.assertTrue(formToFill.isDisplayed());

        //Заполнить имя и проверить, что поле с именем заполнено
        String fieldXPath = "//input[@name = '%s']";
        fillInputField(driver.findElement(By.xpath(String.format(fieldXPath, "userName"))), name);

        //Заполнить номер телефона и проверить, что номер введен
        WebElement phoneField = driver.findElement(By.xpath("//input[@name = 'userTel']"));
        phoneField.sendKeys("9998887765");
        Assertions.assertEquals("+7 (999) 888-7765", phoneField.getAttribute("value"), "Номера телефонов не совпадают");

        //Заполнить адрес электронной почты и проверить, что он введен
        fillInputField(driver.findElement(By.xpath(String.format(fieldXPath, "userEmail"))), "qwertyqwerty");

        //Заполнить адрес и проверить, что адрес введен
        WebElement addressField = driver.findElement(By.xpath("//input[@type = 'text' and contains(@class, 'vue-dadata')]"));
        fillInputField(addressField, "г Пермь, ул Революции, д 6, кв 71");
        WebElement addressToClick = driver.findElement(By.xpath("//span[@class = 'vue-dadata__suggestions-item']"));
        wait.until(ExpectedConditions.elementToBeClickable(addressToClick)).click();
        //addressToClick.click();
        Assertions.assertEquals("г Пермь, ул Революции, д 6, кв 71", addressField.getAttribute("value"), "Адреса не совпадают");

        //Кликнуть на чекбокс и проверить, что он не пуст
        WebElement checkboxParent = driver.findElement(By.xpath("//input[@type='checkbox']/.."));
        WebElement checkbox = checkboxParent.findElement(By.xpath("./p/span[2]/span"));
        scrollToElementJs(checkbox);
        checkbox.click();
        Assertions.assertTrue(checkboxParent.getAttribute("class").contains("is-checked"), "Пустой чекбокс");

        //Кликнуть на 'Свяжитесь со мной'
        WebElement submitButton = driver.findElement(By.xpath("//button[@type = 'submit']"));
        submitButton.click();

        //Найти сообщение о некорректном адресе электронной почты и проверить его
        WebElement wrongEmailMsg = driver.findElement(By.xpath("//div[@formkey = 'email']/div/span"));
        Assertions.assertEquals("Введите корректный адрес электронной почты"
                , wrongEmailMsg.getText()
                , "Сообщение об ошибке отсутствует или отличается от ожидаемого"
        );

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void after() {
        driver.quit();
    }

    private boolean isSwitchToFrame() {
        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("fl-616371")));
            return true;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    private void closeFrameIfSwitched(boolean isSwitchedToFrame) {
        if (isSwitchedToFrame) {
            closeFrame();
        }
    }

    private void closeFrame() {
        WebElement overlayCross = driver.findElement(By.xpath("//div[@data-fl-track = 'click-close-login']"));
        wait.until(ExpectedConditions.elementToBeClickable(overlayCross)).click();
        driver.switchTo().defaultContent();
    }

    private void scrollToElementJs(WebElement element) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillInputField(WebElement element, String value) {
        scrollToElementJs(element);
        element.sendKeys(value);
        boolean check = wait.until(ExpectedConditions.attributeContains(element, "value", value));
        Assertions.assertTrue(check, "Поле было заполнено некорректно");
    }

}
