@staff
Feature: Staff Portal
  As a staff member
  I want to manage service requests
  So that I can process citizen bookings

  Background:
    Given the staff application is running on "http://localhost:8080"

  Scenario: Staff login
    Given I am on the staff portal page
    When I login as staff with username "Staff Member" and password "staff123"
    Then the municipality filter should be visible

  Scenario: Filter requests by municipality
    Given I am on the staff portal page
    And I am logged in as staff "Staff Member" with password "staff123"
    When I select municipality filter "Lisboa"
    And I click "Load Requests"
    Then I should see a list of requests