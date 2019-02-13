Feature: Creating New Step Goals
  Scenario 1: Daily step goal is not met
    Given that user’s initial goal is 5000 steps a day
    When she walks below 5000 steps
    Then she will not be given the option to change her daily goal

  Scenario 2: Accept the optional new goal
    Given that user’s initial goal is 5000 steps a day
    And he or she walks over 5000 steps
    And he or she will be given the option to change her daily goal
    When he or she accepts the optional new goal which is 5500
    Then the new goal of 5500 steps is set
    And he or she will be directed to the home screen

  Scenario 3: Choose to create custom a new goal and higher than before
    Given that user’s initial goal is 5000 steps a day
    And he or she walks over 5000 steps
    And he or she will be given the option to change her daily goal
    When he or she rejects the optional new goal which is 5500
    Then the application asks the user if he or she wants to set custom goal
    When he or she chooses to set custom goal
    Then the application prompt a text-box for new goal input
    When he or she type in 6000 steps
    Then the new goal of 5500 steps is set
    And he or she will be directed to the home screen

  Scenario 4: Choose to create custom a new goal but lower than before
    Given that user’s initial goal is 5000 steps a day
    And he or she walks over 5000 steps
    And he or she will be given the option to change her daily goal
    When he or she rejects the optional new goal which is 5500
    Then the application asks the user if he or she wants to set custom goal
    When he or she chooses to set custom goal
    Then the application prompt a text-box for new goal input
    When he or she type in 4500 steps
    Then the application should say the new goal must be higher than before
    And the application will ask him or her to type in new goal again

  Scenario 5: Declining the new optional goal and custom goal
  Choose to create custom a new goal and higher than before
    Given that user’s initial goal is 5000 steps a day
    And he or she walks over 5000 steps
    And he or she will be given the option to change her daily goal
    When he or she rejects the optional new goal which is 5500
    Then the application asks the user if he or she wants to set custom goal
    When he or she rejects to set custom goal
    Then the goal is not changed and still be 5000 steps
    And he or she will be directed to the home screen
