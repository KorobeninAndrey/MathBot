package mathBot;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/bdd/math-bot-server.feature"},
        tags = {"@math-bot"},
        plugin = {"pretty", "html:target/cucumber-report/math-bot-server", "json:target/cucumber-report/math-bot-server.json"})
public class MathBotServerCucumberTest {

}
