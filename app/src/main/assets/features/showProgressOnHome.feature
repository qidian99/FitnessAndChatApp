Feature: Show Progress on App Home Screen
  Scenario 1 : Sarah walked a few steps while staying on Home Screen
    Given Sarah starts recording her steps after lunch
    And stays on the Home Screen during her walk
    And her initial daily goal is 5000 steps
    When she has finished walking 563 steps
    Then the Home Screen says she has walked 563 steps
    And she has 4437 steps to reach her goal

  Scenario 2 : Sarah walked a few steps while putting her phone away
    Given Sarah starts recording her steps after lunch
    And she locked the phone and put it in her pocket during her walk
    And her initial daily goal is 5000 steps
    When she finishes walking 563 steps and opens her phone
    Then the Home Screen appears
    And shows she has walked 563 steps
    And she has 4437 steps to reach her goal
