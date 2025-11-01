Feature: Citizen Portal
  As a citizen
  I want to use the web portal
  So that I can book and manage service requests

  Background:
    Given the application is running on "http://localhost:8080"

  Scenario: Access homepage
    When I navigate to the homepage
    Then I should see the page title "ZeroMonos - Service Request System"
    And I should see a link to "Citizen Portal"
    And I should see a link to "Staff Portal"

  Scenario: Login successfully
    Given I am on the citizen portal page
    When I login with username "johndoe" and password "password123"
    Then I should see "Welcome, johndoe!"
    And the booking form should be visible

  Scenario: Login with wrong password
    Given I am on the citizen portal page
    When I login with username "johndoe" and password "wrongpassword"
    Then I should see an error message "Authentication failed"

  Scenario: Create a booking
    Given I am on the citizen portal page
    And I am logged in as "johndoe" with password "password123"
    When I select municipality "Lisboa"
    And I select date "2024-12-25"
    And I select time "10:00"
    And I enter description "Need to register new document"
    And I submit the booking
    Then I should see "Request submitted successfully"
    And I should see the generated token

  Scenario: Check request status
    Given I am on the citizen portal page
    And I am logged in as "johndoe" with password "password123"
    And I have created a booking
    When I enter the token in the status check field
    And I click "Check Status"
    Then I should see the request details
    And I should see status "PENDING"

  Scenario: View my requests
    Given I am on the citizen portal page
    And I am logged in as "mariasilva" with password "password123"
    Then I should see the "My Requests" section
    And the requests list should be visible