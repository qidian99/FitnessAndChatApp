Feature: Have online backup data
  Scenario 1: Log in with a new phone
    Given Richard buys a new phone
    And he downloads Personal Best on the new device
    And he logs into his Google account correctly
    And his steps for that week from Sunday to Saturday are (500, 3600, 3500, 4000, 3500, 2000, 5000 steps, respectively)
    And he walked 5000 steps for that same day
    When Personal Best starts up
    And he logged in using the same google account
    Then the app will display 5000 steps for that day
    When he checks his weekly progress,
    Then a bar chart appears and show 500, 3600, 3500, 4000, 3500, 2000, 5000 steps respectively for each day.

  Scenario 2: Log in with a different phone
    Given Richard forgot his phone
    And his friend also has Personal Best
    And his friend doesn’t care too much about recording his stats
    When Richard attempts to log into his friend’s phone
    Then the data from Richard’s account will load over to his friend’s device
    And it will log him out of his previous device (most likely his phone)
    And it will display normally as if it were on Richard’s phone.
