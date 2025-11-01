@citizen
Feature: Citizen Portal
  As a citizen
  I want to use the web portal
  So that I can book and manage service requests

  Background:
    Given the application is running on "http://localhost:8080"

  Scenario: Access homepage
    When I navigate to the homepage
    Then I should see the page title "ZeroMonos - Garbage Collection Services"
    And I should see a link to "Citizen Portal"
    And I should see a link to "Staff Portal"

  Scenario: Login successfully
    Given I am on the citizen portal page
    When I login with username "Maria Silva" and password "maria123"
    Then the booking form should be visible
    And see a logout option

  Scenario: Login with wrong password
    Given I am on the citizen portal page
    When I login with username "johndoe" and password "wrongpassword"
    Then I should see an error message "Invalid username or password"

  Scenario: Create a booking
    Given I am on the citizen portal page
    And I am logged in as "Maria Silva" with password "maria123"
    When I select municipality "Lisboa"
    And I select date "2026-12-25"
    And I select time "10:00"
    And I enter description "Mesa"
    And I submit the booking
    Then I should see "Booking created successfully"
    And I should see the generated token
