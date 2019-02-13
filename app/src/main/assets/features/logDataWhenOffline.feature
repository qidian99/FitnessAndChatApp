Feature: Log data while disconnected from the network
  Scenario 1: Walk while disconnected
    Given that Sarah clicks the start button before a walk
    And she walks to canyon
    And there is no internet connection/data in the canyon
    When she walks into canyon
    Then the Personal Best records her steps
    And the Personal Best shows her steps on screen

  Scenario 2: Connect to the Internet after a disconnected walk
    Given Sarah finish her walk in the canyon
    When she has internet connection again
    Then the Personal Best app uploads her data to the cloud
