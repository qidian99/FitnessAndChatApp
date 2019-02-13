Feature: Changing step goal at any time
  Scenario 1: Sarah input new valid goal
    Given Sarah has averaged 4000 steps per day for the week
    And her current goal is 5000 steps per day
    And she press “changing step goal”
    When she set her goal to 4500 steps per day a week
    Then application will show that her new goal of 4500 is set
    And she will be directed to the home screen
  Scenario 2: Sarah input invalid valid goal
    Given Sarah has averaged 4000 steps per day for the week
    And her current goal is 5000 steps per day
    And she press “changing step goal”
    When she set her goal to 0 steps per day a week
    Then application will show that her new goal of 0 is invalid
    And she will be asked to input a valid goal
