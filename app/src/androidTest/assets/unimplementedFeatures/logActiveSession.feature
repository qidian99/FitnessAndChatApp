Feature: Way to Log intended active session (Start/End Button)
  Scenario 1: User presses start button
    Given that Sarah wants to start her workout
    And she has the app started and running
    When she presses the Start Walk/Run button
    Then a new session will activate
    And will record her steps starting at 0.

  Scenario 2: User presses end button
    Given that Sarah had started her workout
    And had the app running with an active session
    When she presses the End Walk/Run button
    Then the current session will end and display her stats
