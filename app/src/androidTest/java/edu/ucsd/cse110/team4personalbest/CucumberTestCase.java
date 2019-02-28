package edu.ucsd.cse110.team4personalbest;

import junit.framework.TestCase;

import cucumber.api.CucumberOptions;

@CucumberOptions(glue = "edu.ucsd.cse110.googlefitapp.cucumber.steps", features = "features")
public class CucumberTestCase extends TestCase {
}