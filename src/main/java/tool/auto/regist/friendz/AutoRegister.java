package tool.auto.regist.friendz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AutoRegister {

	private static final String EMAIL = "celedionmyhearwillgoon99999999";
	private static final String EMAIL_PASS = "Dragon0104146890";

	private static final int PHONE_NUM_LENGTH = 11;

	private String refUrl;
	private int fromIndex;
	private int toIndex;
	List<String> emailsList;
	List<String> inputNamesList;
	private WebDriver driver;
	
	public AutoRegister(String refUrl, int fromIndex, int toIndex, List<String> emailsList,
			List<String> inputNamesList, WebDriver driver) {
		this.refUrl = refUrl;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
		this.emailsList = emailsList;
		this.inputNamesList = inputNamesList;
		this.driver = driver;
	}

	public void run() {
		autoRegister(refUrl, emailsList, inputNamesList, fromIndex, toIndex);
	}
	
	/**
	 * Using FirefoxDriver to control register process
	 * 
	 * @param refUrl
	 * @param emailsList
	 * @param inputNamesList
	 * @param fromIndex
	 * @param toIndex
	 */
	private void autoRegister(String refUrl, List<String> emailsList,
			List<String> inputNamesList, int fromIndex, int toIndex) {

		try {

			if (emailsList.size() > 0) {
				int numOfAcc = 0;
				String email;

				// Go to web
				driver.get(refUrl);
				while (!("https://friendz.io/").equals(driver.getCurrentUrl())) {
					TimeUnit.SECONDS.sleep(3);
				}

				driver.get("https://steward.friendz.io/register");
				while (!("https://steward.friendz.io/register").equals(driver.getCurrentUrl())) {
					TimeUnit.SECONDS.sleep(3);
				}

				for (int i = fromIndex; i < toIndex; i++) {
					email = emailsList.get(i);
					try {
						fillRegistForm(inputNamesList, email, driver);
						while (!("https://steward.friendz.io/waitVerification").equals(driver.getCurrentUrl())) {

							if (existsElement("has-error", false, driver)) {
								if (i < emailsList.size() - 1) {
									email = emailsList.get(++i);
									fillRegistForm(inputNamesList, email, driver);
									TimeUnit.SECONDS.sleep(3);
								} else {
									break;
								}
							}
						}
						numOfAcc++;

						// Confirm mail
						if (numOfAcc == 10) {
							confirmMail(driver);
							numOfAcc = 0;
						}

						driver.get("https://steward.friendz.io/logout");
						while (!("https://steward.friendz.io/login").equals(driver.getCurrentUrl())) {
							TimeUnit.SECONDS.sleep(3);
						}

						driver.get("https://steward.friendz.io/register");
						while (!("https://steward.friendz.io/register").equals(driver.getCurrentUrl())) {
							TimeUnit.SECONDS.sleep(2);
						}
					} catch (Exception e) {
						System.out.println("==== ERROR email: " + email + ": " + e);
						writeErrorMessages(email);
						
						driver.get("https://steward.friendz.io/logout");
						while (!("https://steward.friendz.io/login").equals(driver.getCurrentUrl())) {
							TimeUnit.SECONDS.sleep(3);
						}

						driver.get("https://steward.friendz.io/register");
						while (!("https://steward.friendz.io/register").equals(driver.getCurrentUrl())) {
							TimeUnit.SECONDS.sleep(2);
						}
					}
				}
			}

			// Confirm mail
			confirmMail(driver);
			System.out.println("=== DONE: " + refUrl);
		} catch(Exception e) {
			System.out.println("Exception: " + e);
		}
	}

	private void fillRegistForm(List<String> inputNamesList, String email, WebDriver driver) {

		WebElement element;
		element = driver.findElement(By.name("name"));
		element.clear();
		element.sendKeys(getRandomName(inputNamesList));

		element = driver.findElement(By.name("email"));
		element.clear();
		element.sendKeys(email);

		element = driver.findElement(By.name("phone"));
		element.clear();
		element.sendKeys(getRandomPhoneNum(PHONE_NUM_LENGTH));

		element = driver.findElement(By.name("country"));
		element.sendKeys("DE - GERMANY");

		element = driver.findElement(By.name("password"));
		element.clear();
		element.sendKeys(email);

		element = driver.findElement(By.name("password_confirmation"));
		element.clear();
		element.sendKeys(email);
		element.submit();
	}

	/**
	 * Login to gmail to confirm mail
	 * 
	 * @throws InterruptedException
	 */
	private void confirmMail(WebDriver driver) throws InterruptedException {

		String confirmPath;
		String selectLinkOpeninNewTab = Keys.chord(Keys.CONTROL, Keys.RETURN);
		List<String> tabList;
		List<WebElement> confirmPathsList;

		try {
			// Open Gmail
			driver.get("https://mail.google.com");
			TimeUnit.SECONDS.sleep(2);
			if (!"https://mail.google.com/mail/u/0/#inbox".equals(driver.getCurrentUrl())) {
				WebElement gmail = driver.findElement(By.id("identifierId"));
				gmail.sendKeys(EMAIL);
				driver.findElement(By.id("identifierNext")).click();

				TimeUnit.SECONDS.sleep(2);
				WebElement gmailPass = driver.findElement(By.name("password"));
				gmailPass.sendKeys(EMAIL_PASS);
				driver.findElement(By.id("passwordNext")).click();
			}

			TimeUnit.SECONDS.sleep(2);
			List<WebElement> unReadMailList = driver.findElements(By.xpath("//*[@class='zF']"));
			int numOfFriendzMail = 0;
			int numOfConfirmedFol = 0;
			for (WebElement element : unReadMailList) {
				if (element.isDisplayed() && "FriendzDashboard".equals(element.getText())) {
					numOfFriendzMail++;
				}
			}

			for (WebElement element : unReadMailList) {
				if (element.isDisplayed() && "FriendzDashboard".equals(element.getText())) {

					// Read confirm mail
					element.click();
					TimeUnit.SECONDS.sleep(2);
					confirmPathsList = driver.findElements(By.tagName("a"));

					for (WebElement k : confirmPathsList) {
						if (k.getText().indexOf("https://steward.friendz.io/verificationUser") != -1) {
							confirmPath = k.getText();
							driver.findElement(By.linkText(confirmPath)).sendKeys(selectLinkOpeninNewTab);
							TimeUnit.SECONDS.sleep(1);
						}
					}

					// Close tabs that is used to confirm mail except main tab
					tabList = new ArrayList<String>();
					tabList.addAll(driver.getWindowHandles());
					while(tabList.size() > 1) {
						driver.switchTo().window(tabList.get(1));
						TimeUnit.SECONDS.sleep(2);
						driver.close();
						driver.switchTo().window(tabList.get(0));
						tabList.remove(1);
					}

					if (++numOfConfirmedFol < numOfFriendzMail) {
						confirmMail(driver);
					}
				}
			}
		} catch (Exception e) {
			confirmMail(driver);
		}
	}

	/**
	 * Write error messages to error.log file
	 * 
	 * @param errorMsg 
	 */
	public void writeErrorMessages(String errorMsg) {

		BufferedWriter bufferedWriter = null;
		FileWriter fileWriter = null;
		BufferedReader bufferedReader = null;
		FileReader fileReader = null;

		try {
			String currentLine;
			StringBuilder fileContent = new StringBuilder();

			Path filePath = Paths.get("src", "main", "resources", "error_mail.csv");
			File logFile = new File(filePath.toString());

			// If the log file existed then read content of file, after that append new content into that one.
			if (logFile.length() > 0) {
				fileReader = new FileReader(logFile);
				bufferedReader = new BufferedReader(fileReader);

				while ((currentLine = bufferedReader.readLine()) != null) {
					if (currentLine != null) {
						fileContent.append(currentLine + "\n");
					}
				}
			}

			fileContent.append(errorMsg + "\n");

			fileWriter = new FileWriter(logFile);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(fileContent.toString());
		} catch (IOException e) {
		} finally {
			try {
				if (bufferedWriter != null) {
					bufferedWriter.close();
				}
				if (fileWriter != null) {
					fileWriter.close();
				}
				if (fileReader != null) {
					fileReader.close();
				}
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException ex) {
			}
		}
	}
	

	private String getRandomPhoneNum(int length) {

		String numChar = "0123456789";
		StringBuilder phoneNum = new StringBuilder("0");
		Random rand = new Random();
		char charAt;

		for (int i = 0; i < length; i++) {
			charAt = numChar.charAt(rand.nextInt(numChar.length()));
			while (i == 0 && charAt == '0') {
				charAt = numChar.charAt(rand.nextInt(numChar.length()));
			}
			phoneNum.append(charAt);
		}

		return phoneNum.toString();
	}

	private static String getRandomName(List<String> inputNamesList) {
		Random rand = new Random();
		int nameIndex = rand.nextInt(inputNamesList.size());
		int lastIndex = rand.nextInt(inputNamesList.size());
		return inputNamesList.get(nameIndex) + " " + inputNamesList.get(lastIndex);
	}

	private boolean existsElement(String name, boolean isId, WebDriver driver) {

		try {
			if (isId) {
				driver.findElement(By.id(name));
			} else {
				driver.findElement(By.className(name));
			}
		} catch (NoSuchElementException e) {
			return false;
		}
		return true;
	}
}