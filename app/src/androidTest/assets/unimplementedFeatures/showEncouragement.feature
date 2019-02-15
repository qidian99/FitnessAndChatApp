Feature: Encouragement
  Scenario 1: User increases his average daily step counts by is not over the goal
    Given Richard’s initial goal is 5000 steps a day
    And if Richard walked about 1800 steps a day
    When Richard walks about 3000 steps a day
    Then the app tells Richard that “you have increased his daily steps by over 1000 steps” to encourage him to walk more often

  Scenario 2: User increases his average daily step counts and is over the goal
    Given Richard’s initial goal is 5000 steps a day
    And if Richard walked about 4000 steps a day
    When Richard walks about 5500 steps a day
    Then the app tells Richard that “Congratulations! Do you want to set a new step goal?”
    And Richard can choose whether or not to create a new step goal
