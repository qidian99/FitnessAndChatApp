Feature: Manually Record Steps Taken
  Note that since users are normally poor at estimation, this should be based on history data. However, users might use this to cheat on their workouts
  Scenario 1: User enters a valid number to manually record the steps taken
    Given that Richard went out for a walk
    And that he forgot his phone at home
    And after returning from the walk and reviewing his history steps, Richard came up with an estimate of 2000 steps today
    And his previous steps are ranging from 500 - 3500 steps
    When he input the estimate of 2000,
    Then the Home Screen says that he has walked 2000 steps today.

  Scenario 2: User enters an invalid number when manually recording the steps taken
    Given that Richard went out for a walk
    And that he forgot his phone at home
    And after returning from the walk and reviewing his history steps, Richard came up with an estimate of 2000 steps today
    And his previous steps are ranging from 500 - 3500 steps
    When he input the estimate of 0,
    Then the Home Screen says that he cannot input 0 as estimated steps
    And Home Screen will ask him to input a valid number

  Scenario 3: User enters a too high estimation when manually recording the steps taken
    Given that Richard went out for a walk
    And that he forgot his phone at home
    And after returning from the walk and reviewing his history steps, Richard came up with an estimate of 10000 steps today
    And his previous steps are ranging from 500 - 3500 steps
    When he input the estimate of 10000,
    Then the Home Screen says that he cannot input 10000 as estimated steps because it is too high compared to his highest step record for the week, which is 3500 steps
    And Home Screen will ask him to input a reasonable number

