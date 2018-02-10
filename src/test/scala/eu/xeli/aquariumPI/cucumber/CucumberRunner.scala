package eu.xeli.aquariumPI.cucumber

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  glue = Array("eu.xeli.aquariumPI.cucumber.steps"),
  plugin = Array("pretty", "html:target/cucumber/html"),
  features = Array("classpath:features")
)
class CucumberRunner {
}