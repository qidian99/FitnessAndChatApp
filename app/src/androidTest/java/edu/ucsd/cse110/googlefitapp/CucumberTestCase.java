package edu.ucsd.cse110.googlefitapp;

import junit.framework.TestCase;

import cucumber.api.CucumberOptions;

@CucumberOptions(glue = "edu.ucsd.cse110.googlefitapp.cucumber.steps", features = "features")
public class CucumberTestCase extends TestCase {
}