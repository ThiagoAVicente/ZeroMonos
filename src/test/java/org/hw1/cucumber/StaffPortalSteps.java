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

public class StaffPortalSteps {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private String baseUrl;

    @Before("@staff")
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

    @After("@staff")
    public void tearDown(Scenario scenario) {
        if (page != null) {
            page.close();
        }

        if (context != null) {
            context.close();
        }
    }

    @Given("the staff application is running on {string}")
    public void theStaffApplicationIsRunningOn(String url) {
        this.baseUrl = url;
    }

    @Given("I am on the staff portal page")
    public void iAmOnTheStaffPortalPage() {
        page.navigate(baseUrl + "/staff");
        page.waitForLoadState();
    }

    @When("I login as staff with username {string} and password {string}")
    public void iLoginAsStaffWithUsernameAndPassword(String username, String password) {
        page.fill("#loginUsername", username);
        page.fill("#loginPassword", password);
        page.click("button[type='submit']:has-text('Login')");
        page.waitForTimeout(1000);
    }

    @Then("the municipality filter should be visible")
    public void theMunicipalityFilterShouldBeVisible() {
        assertThat(page.locator("#municipalityFilter").isVisible()).isTrue();
    }

    @Given("I am logged in as staff {string} with password {string}")
    public void iAmLoggedInAsStaffWithPassword(String username, String password) {
        iLoginAsStaffWithUsernameAndPassword(username, password);
        page.waitForSelector("#municipalityFilter", new Page.WaitForSelectorOptions().setTimeout(5000));
    }

    @When("I select municipality filter {string}")
    public void iSelectMunicipalityFilter(String municipality) {
        page.selectOption("#municipalityFilter", municipality);
    }

    @And("I click {string}")
    public void iClick(String buttonText) {
        page.click("button:has-text('" + buttonText + "')");
        page.waitForTimeout(1000);
    }

    @Then("I should see a list of requests")
    public void iShouldSeeAListOfRequests() {
        assertThat(page.locator("#tableSection").isVisible()).isTrue();
    }
}
