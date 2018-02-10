Feature: Automatic top off
  Scenario: The waterpump is enabled when the water sensor gets triggered
    Given a valid config
    And the water level sensor is set at 1 and ato pump at 2
    When the application starts
    And a gpio update is send for pin 1 with level 1
    Then pin 2 is set to high
