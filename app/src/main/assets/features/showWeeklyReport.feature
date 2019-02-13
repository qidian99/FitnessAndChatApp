Feature: Achieving Daily Goals (Weekly Report)
  Scenario 1: Sarah achieved the goal on some days
    Given today is Saturday,
    And Sarah has walked 3500, 3600, 3500, 4000, 3500, 2000, 5000 steps, respectively, on each day since last Sunday
    And her goal is 4000 steps (so she made it on Wednesday and Saturday)
    When she presses the bar graph button (“Show Graph”)
    Then the bar graph representing the current week statistics will show up
    And the current goal line will appear to reveal that she has met the goal for 2 days
    And the bars will be proportional to the steps she walked throughout the week (500, 3600, 3500, 4000, 3500, 2000, 5000 steps, respectively)
    And the bars for Wednesday and Saturday have check marks on top

  Scenario 2: Sarah did not meet the goal on any day
    Given today is Saturday,
    And Sarah has walked 3500, 3600, 3500, 4000, 3500, 2000, 5000 steps, respectively, on each day since last Sunday
    And her goal is 9000 steps (so she did not meet the goal on any day)
    When she presses the bar graph button (“Show Graph”)
    Then the bar graph representing the current week statistics will show up
    And the current goal line will appear to reveal that she has met the goal for 0 days
    And the bars will be proportional to the steps she walked throughout the week (500, 3600, 3500, 4000, 3500, 2000, 5000 steps, respectively)
    And all bars do not have check marks on top
