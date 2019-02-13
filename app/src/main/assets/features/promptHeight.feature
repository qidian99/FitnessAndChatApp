Feature: Prompt for Height

  Scenario 1: User uses feet and inches
    Given Sarah has successfully downloaded the app
    And she has accepted all the permissions
    And she uses feet and inches for her height
    When the application asks for her height, she
    Then chooses the feet and inches option in the drop-down menu
    And inputs 5 in the first textbox and 4 in the second textbox
    When she presses the “Done” button
    Then she is taken to the home screen.

  Scenario 2: User uses centimeter
    Given Richard has successfully downloaded the app following a google search
    And has accepted all permissions by checking the “OK” boxes
    And he uses centimeter for his height
    When the application asks for his height, he
    Then chooses the centimeters option in the drop-down menu
    And input a value of 160 in the textbox
    When he presses the “Done” button
    Then it took him took the home screen

  Scenario 3: User type invalid height (0 or negative number)
    Given Sarah has successfully downloaded the app
    And she has accepted all the permissions
    And she uses feet and inches for her height
    When the application asks for her height, she
    Then chooses the feet and inches option in the drop-down menu
    And inputs 0 in the first textbox and 0 in the second textbox
    When she presses the “Done” button
    Then the application should say height is invalid
    And the application will ask her to type appropriate height

  @skipAndroid
