Feature: Staff Portal
  As a staff member
  I want to manage service requests
  So that I can process citizen bookings

  Background:
    Given the application is running on "http://localhost:8080"

  Scenario: Staff login
    Given I am on the staff portal page
    When I login with username "staff" and password "staff123"
    Then I should see "Welcome, staff!"
    And the municipality filter should be visible

  Scenario: Filter requests by municipality
    Given I am on the staff portal page
    And I am logged in as "staff" with password "staff123"
    When I select municipality filter "Lisboa"
    And I click "Load Requests"
    Then I should see a list of requests

  Scenario: View request details
    Given I am on the staff portal page
    And I am logged in as "staff" with password "staff123"
    When I load requests for "Porto"
    And I click on the first request
    Then I should see the request details panel
    And I should see the request history

  Scenario: Update request status
    Given I am on the staff portal page
    And I am logged in as "staff" with password "staff123"
    When I load requests for "Lisboa"
    And I view the request details
    And I update the status to "APPROVED"
    Then I should see "Status updated successfully"