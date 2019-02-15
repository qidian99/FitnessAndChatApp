Feature: A Collection of Records for Progress Feedback
  We consider (Sunday -> Saturday) to be a week
  Scenario 1: Show current week history at the end of a week
    Given that Sarah has completed her workout(s) for the week
    And today is Saturday
    And she walked 3500, 3600, 3500, 4000, 3500, 2000, 5000 steps, respectively, on each day since last Sunday
    When she opens the app and presses the (“Show Current Week”) button,
    Then it will reveal her stats for the current week, with each day’s statistics presented on screen consistently (3500, 3600, 3500, 4000, 3500, 2000, 5000 steps)

  Scenario 2: Show current week history in the midst of a week
    Given that today is Monday and she has walked 250 steps
    And she walked 3500 steps on Sunday
    When she opens the app and presses the (“Show Current Week”) button,
    Then it will reveal her stats for the current week such that there is 3500 steps for Sunday, 250 steps for today (Monday), and 0 step for all other days of the week.

  Scenario 3: Show current week history at the beginning of a week
    Given that Sarah has completed her workout(s) for the week
    And she walked 3500, 3600, 3500, 4000, 3500, 2000, 5000 steps, respectively, on each day since last Sunday
    And the clock just turned to 0:00 on Sunday
    When she opens the app and presses the (“Show Current Week”) button,
    Then it will show 0 step for all days, since a new week has just started.
