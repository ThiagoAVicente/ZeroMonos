package org.hw1.cucumber;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class CitizenPortalSteps {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private String baseUrl;

    @Before("@citizen")
    public void setUp() {
        if (playwright == null) {
            playwright = Playwright.create();
        }

        if (browser == null) {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true));
        }

        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 720));

        page = context.newPage();
    }

    @After("@citizen")
    public void tearDown(Scenario scenario) {

        if (page != null) {
            page.close();
        }

        if (context != null) {
            context.close();
        }
    }

    @Given("the application is running on {string}")
    public void theApplicationIsRunningOn(String url) {
        this.baseUrl = url;
    }

    @When("I navigate to the homepage")
    public void iNavigateToTheHomepage() {
        page.navigate(baseUrl);
    }

    @Then("I should see the page title {string}")
    public void iShouldSeeThePageTitle(String expectedTitle) {
        String actualTitle = page.title();
        assertThat(actualTitle).isEqualTo(expectedTitle);
    }

    @And("I should see a link to {string}")
    public void iShouldSeeALinkTo(String linkText) {
        assertThat(page.getByText(linkText).isVisible()).isTrue();
    }

    @Given("I am on the citizen portal page")
    public void iAmOnTheCitizenPortalPage() {
        page.navigate(baseUrl + "/citizen");
        page.waitForLoadState();
    }

    @When("I login with username {string} and password {string}")
    public void iLoginWithUsernameAndPassword(String username, String password) {
        page.fill("#loginUsername", username);
        page.fill("#loginPassword", password);
        page.click("button[type='submit']:has-text('Login')");
        page.waitForTimeout(1000);
    }

    @Then("the booking form should be visible")
    public void theBookingFormShouldBeVisible() {
        assertThat(page.locator("#bookingForm").isVisible()).isTrue();
    }

    @And("see a logout option")
    public void seeALogoutOption() {
        assertThat(page.getByText("Logout").isVisible()).isTrue();
    }

    @Then("I should see an error message {string}")
    public void iShouldSeeAnErrorMessage(String errorMessage) {
        String alertText = page.locator(".alert-error").textContent();
        assertThat(alertText).contains(errorMessage);
    }

    @Given("I am logged in as {string} with password {string}")
    public void iAmLoggedInAsWithPassword(String username, String password) {
        iLoginWithUsernameAndPassword(username, password);
        page.waitForSelector("#bookingForm", new Page.WaitForSelectorOptions().setTimeout(5000));
    }

    @When("I select municipality {string}")
    public void iSelectMunicipality(String municipality) {
        page.selectOption("select[name='municipality'], #municipality", municipality);
    }

    @And("I select date {string}")
    public void iSelectDate(String date) {
        page.fill("input[type='date'], input[name='date'], #date", date);
    }

    @And("I select time {string}")
    public void iSelectTime(String time) {
        page.fill("input[type='time'], input[name='time'], #time", time);
    }

    @And("I enter description {string}")
    public void iEnterDescription(String description) {
        page.fill("textarea[name='description'], #description", description);
    }

    @And("I submit the booking")
    public void iSubmitTheBooking() {
        page.click("button[type='submit']:has-text('Book Collection')");
        page.waitForTimeout(2000);
    }

    @Then("I should see {string}")
    public void iShouldSee(String expectedText) {
        assertThat(page.content()).contains(expectedText);
    }

    @And("I should see the generated token")
    public void iShouldSeeTheGeneratedToken() {
        String tokenElement = page.locator("#generatedToken").textContent();
        assertThat(tokenElement).isNotNull();
    }

    @Then("I should see the {string} section")
    public void iShouldSeeTheSection(String sectionName) {
        Locator h2 = page.locator("h2", new Page.LocatorOptions().setHasText(sectionName));
        assertThat(h2.isVisible()).isTrue();
    }

    @And("the requests list should be visible")
    public void theRequestsListShouldBeVisible() {
        assertThat(page.locator("#requestsList, .requests-list, [data-testid='requests-list']").isVisible()).isTrue();
    }
}
