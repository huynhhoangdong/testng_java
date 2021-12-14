package test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import helper.SpreadSheetIntegration;
import io.github.bonigarcia.wdm.WebDriverManager;

public class SpreadSheetTest {
    private WebDriver driver;
    private SpreadSheetIntegration spreadsheet;
    private String email, userID, password;

    @BeforeClass
    public void beforeClass() throws IOException, GeneralSecurityException {
        WebDriverManager.chromedriver().setup(); //tự động nhận diện và tải về Chrome driver
        driver = new ChromeDriver();

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); //Set Global implicit wait time
        driver.manage().window().maximize();

        //Khởi tạo lớp SpreadsheetIntegration
        spreadsheet = new SpreadSheetIntegration();

        //Đầu tiên tạo 1 google sheet mới để ghi và đọc thông tin
        spreadsheet.createANewSheet();
    }

    @Test
    public void step1_RegisterANewAccount() throws IOException {
        //Truy cập site demo
        driver.get("http://demo.guru99.com/v4/");

        //Click vào link để đăng ký account
        driver.findElement(By.xpath("//a[text()='here']")).click();

        //Điền email để đăng ký account
        email = "tam" + randomNumber() + "@mail.com";
        driver.findElement(By.name("emailid")).sendKeys(email);

        //Nhấn submit để đăng ký account
        driver.findElement(By.name("btnLogin")).click();

        //Lấy userID đã được generate
        userID = driver.findElement(By.xpath("//td[text()='User ID :']/following-sibling::td")).getText();

        //Lấy password đã được generate
        password = driver.findElement(By.xpath("//td[text()='Password :']/following-sibling::td")).getText();

        //Ghi tiếp giá trị userID và password và sheet vừa tạo
        spreadsheet.appendDataToSpreadSheet(userID, password);
    }

    @Test
    public void step2_loginWithDataSet() throws IOException {
        List<String> dataSet = spreadsheet.readDataFromSpreadSheet();
        for(int i=0; i<dataSet.size(); i++){
            driver.get("http://demo.guru99.com/v4/");
            String[] userInfo = dataSet.get(i).split(" ");
            String userId = userInfo[0];
            String password = userInfo[1];
            driver.findElement(By.name("uid")).sendKeys(userId);
            driver.findElement(By.name("password")).sendKeys(password);
            driver.findElement(By.name("btnLogin")).click();
            String welcomeMsg = driver.findElement(By.xpath("//tr[@class='heading3']/td")).getText();
            Assert.assertTrue(welcomeMsg.contains(userId));
        }
    }

    @AfterTest
    public void afterTest(){
        driver.quit();
    }

    /**
     * Hàm tạo số random
     * @return số random
     */
    public int randomNumber(){
        Random random = new Random();
        int randomNumber = random.nextInt(999);
        return randomNumber;
    }
}