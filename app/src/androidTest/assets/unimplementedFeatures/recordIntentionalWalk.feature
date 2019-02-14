Feature: Record of Intentional Walk
  Scenario 1 : Sarah Ends Her active walk
    Given that Sarah has walked 1.5 miles, 3000 steps, and 60 minutes on current active walk
    And she her steps taken for the rest of the day is 500 for 0.3 miles
    When she presses the end button to end her active walk
    Then the app takes her back to the home screen
    And shows she has taken 3000 steps for the current session and 3500 steps in total
    And shows her speed is 1.5 MPH
    And shows she has walked 1.5 miles for a total of 1.8 miles for the day
    And the time elapsed is 60 minutes

  Scenario 2 : Sarah Go on an active walk for the second time
    Given that Sarah has walked 1.5 miles, 3000 steps, and 60 minutes on her previous active walk
    And she her steps taken for the rest of the day is 500 for 0.3 miles
    And for the current session, Sarah has walked 1 mile, 2000 steps, and 30 minutes
    When she presses the end button to end her active walk
    Then the app takes her back to the home screen
    And shows she has taken 2000 steps for the current session and 5500 steps in total
    And shows her speed is 2 MPH
    And shows she has walked 1 miles for a total of 2.8 miles for the day
    And the time elapsed is 30 minutes
