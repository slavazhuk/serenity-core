package net.serenitybdd.junit.runners.integration;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestResult;
import net.thucydides.junit.rules.DisableThucydidesHistoryRule;
import net.thucydides.junit.rules.QuietThucydidesLoggingRule;
import net.thucydides.junit.runners.AbstractTestStepRunnerTest;
import net.thucydides.samples.LongSamplePassingScenarioUsingHTMLUnit;
import net.thucydides.samples.LongSamplePassingScenarioUsingPhantomJS;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class WhenRunningATestScenarioWithScreenshots extends AbstractTestStepRunnerTest {

    @Rule
    public QuietThucydidesLoggingRule quietThucydidesLoggingRule = new QuietThucydidesLoggingRule();

    @Rule
    public DisableThucydidesHistoryRule disableThucydidesHistoryRule = new DisableThucydidesHistoryRule();

    @Test
    public void the_test_runner_records_the_steps_as_they_are_executed_with_phantom_js() throws InitializationError {

        SerenityRunner runner = new SerenityRunner(LongSamplePassingScenarioUsingPhantomJS.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(1));
        TestOutcome testOutcome1 = executedSteps.get(0);

        assertThat(testOutcome1.getTitle(), is("Happy day scenario"));
        assertThat(testOutcome1.getName(), is("happy_day_scenario"));
        assertThat(testOutcome1.getTestSteps().size(), is(3));
        assertThat(testOutcome1.getScreenshots().size(), is(not(0)));
        assertThat(testOutcome1.getResult(), is(TestResult.SUCCESS));
    }

    @Test
    @Ignore
    public void the_test_runner_records_the_steps_as_they_are_executed_with_html_unit() throws InitializationError {
        SerenityRunner runner = new SerenityRunner(LongSamplePassingScenarioUsingHTMLUnit.class);
        runner.run(new RunNotifier());

        List<TestOutcome> executedSteps = runner.getTestOutcomes();
        assertThat(executedSteps.size(), is(1));
        TestOutcome testOutcome1 = executedSteps.get(0);

        assertThat(testOutcome1.getTitle(), is("Happy day scenario"));
        assertThat(testOutcome1.getName(), is("happy_day_scenario"));
        assertThat(testOutcome1.getTestSteps().size(), is(3));
        assertThat(testOutcome1.getScreenshots().size(), is(0));
        assertThat(testOutcome1.getResult(), is(TestResult.SUCCESS));
    }
}
