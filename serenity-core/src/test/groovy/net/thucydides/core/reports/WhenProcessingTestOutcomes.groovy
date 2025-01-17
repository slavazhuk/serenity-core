package net.thucydides.core.reports

import net.thucydides.core.model.TestOutcome
import net.thucydides.core.model.TestResult
import net.thucydides.core.model.TestTag
import net.thucydides.core.util.EnvironmentVariables
import net.thucydides.core.util.MockEnvironmentVariables
import spock.lang.Specification

import static net.thucydides.core.reports.matchers.TestOutcomeMatchers.*
import static net.thucydides.core.util.TestResources.directoryInClasspathCalled
import static org.junit.matchers.JUnitMatchers.everyItem

class WhenProcessingTestOutcomes extends Specification {

    def loader = new TestOutcomeLoader()

    def "should load test outcomes from a given directory"() {
        when:
            List<TestOutcome> testOutcomes = loader.forFormat(OutcomeFormat.XML).loadFrom(directoryInClasspathCalled("/tagged-test-outcomes"));
        then:
            testOutcomes.size() == 3
    }

    EnvironmentVariables environmentVariables = new MockEnvironmentVariables()

    def "should load tests in JSON if configured"() {
        given:
            environmentVariables.setProperty("thucydides.report.format","json");
            def loader = new TestOutcomeLoader(environmentVariables)
        when:
            List<TestOutcome> testOutcomes = loader.forFormat(OutcomeFormat.JSON).loadFrom(directoryInClasspathCalled("/json-test-outcomes"));
        then:
            testOutcomes.size() == 11
    }

    def "should not load test outcome from an invalid directory"() {
        when:
            loader.loadFrom(new File("/does-not-exist"))
        then:
            thrown ReportLoadingFailedError
    }

    def "should list all the tag types for the test outcomes"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tagTypes = testOutcomes.getTagTypes()
        then:
            tagTypes == ["epic", "feature", "story"]
    }

    def "should list all the tags for the test outcomes"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tags = testOutcomes.getTagNames()
        then:
            tags == ["a feature", "a story", "an epic", "another different story", "another story"]
    }

    def "should list all the tags of a given type for the test outcomes"() {
        given:
        TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
        def tags = testOutcomes.getTagsOfType('story')
        then:
        tags.collect({it.shortName}) as Set == ["another story", "a story", "another different story"] as Set
    }

    def "should list all the tags of a given type for the test outcomes except for specified tags"() {
        given:
        TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
        def tags = testOutcomes.getTagsOfTypeExcluding 'story', 'a story'
        then:
        tags.collect({it.shortName}) as Set  == ["another different story", "another story"] as Set
    }

    def "should list all the tags of a given type for the test outcomes except for specified tags for different cases"() {
        given:
        TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
        def tags = testOutcomes.getTagsOfTypeExcluding 'story', 'A Story'
        then:
        tags.collect({it.shortName}) as Set  == ["another different story", "another story"] as Set
    }

    def "should list all the tags of a single type for the test outcomes"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tags = testOutcomes.getTagsOfType 'feature'
        then:
            tags.collect({it.shortName}) == ["A Feature"]
    }

    def "should list all the tags for a given type"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            Set<String> tagTypes = testOutcomes.withTagType("feature").getTagTypes()
        then:
            tagTypes == ["feature","story"] as Set
    }

    def "should list all the tag types for a given name"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            Set<String> tagTypes = testOutcomes.withTag("an epic").getTagTypes()
        then:
            tagTypes == ["epic","story"] as Set
    }

    def "should list tests in alphabetical order"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tests = testOutcomes.getTests()
        then:
            tests.size() == 3
            tests[0].getTitle() <= tests[1].getTitle()
            tests[1].getTitle() <= tests[2].getTitle()
    }

    def "should list tests for a given tag type"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tests = testOutcomes.withTagType("feature").getTests()
        then:
            tests everyItem(havingTagType("feature"))
    }

    def "should list tests for a given issue using a tag notation"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tests = testOutcomes.withTag(TestTag.withValue("issue:ISSUE-1")).getTests()
        then:
            tests.size() == 1 && tests[0].hasIssue("ISSUE-1")
    }

    def "should list tests for a given set of tags"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tags = [TestTag.withValue("story:a story"), TestTag.withValue("story:another story")]
            def tests = testOutcomes.withTags(tags).getTests()
        then:
            tests.each { test ->
                assert test.tags.contains(TestTag.withValue("story:a story")) || test.tags.contains(TestTag.withValue("story:another story"))
            }
    }

    def "should list tests for a given set of issue numbers"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tags = [TestTag.withValue("issue:ISSUE-1"), TestTag.withValue("issue:ISSUE-2")]
            def tests = testOutcomes.withTags(tags).getTests()
        then:
            tests.each { test ->
                assert test.hasIssue("ISSUE-1") || test.hasIssue("ISSUE-2")
            }
    }

    def "should list tests for a given tag"() {
        given:
        TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
        def tests = testOutcomes.withTag("a story").getTests()
        then:
        tests everyItem(havingTagName("a story"))
    }


    def "should list all passing tests"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/test-outcomes/containing-failure"));
        when:
            def tests = testOutcomes.passingTests.getTests()
        then:
            tests.size() == 1
            tests everyItem(withResult(TestResult.SUCCESS))
    }

    def "should list all passing tests from JSON files"() {
        given:
            environmentVariables.setProperty("thucydides.report.format","json");
            def loader = new TestOutcomeLoader(environmentVariables)
            TestOutcomes testOutcomes = TestOutcomes.of(loader.loadFrom(directoryInClasspathCalled("/json-test-outcomes")));
        when:
            def tests = testOutcomes.passingTests.getTests()
        then:
            tests.size() == 11
    }

    def "should list all failing tests"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/test-outcomes/containing-failure"));
        when:
            def tests = testOutcomes.failingTests.getTests()
        then:
            tests.size() == 1
            tests everyItem(withResult(TestResult.FAILURE))
    }

    def "should list all pending tests"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/test-outcomes/containing-failure"));
        when:
            def tests = testOutcomes.pendingTests.getTests()
        then:
            tests.size() == 1
            tests everyItem(withResult(TestResult.PENDING))
    }

    def "should list tests for a given tag and tag type"() {
        given:
            TestOutcomes testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        when:
            def tests = testOutcomes.withTagType("feature").withTag("a feature").getTests()
        then:
            tests everyItem(havingTagName("a feature"))
    }

    def "should provide total test duration for a set of tests"() {
        when:
            def testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        then:
            testOutcomes.duration == 1775
    }

    def "should provide total test duration in seconds for a set of tests"() {
        when:
            def testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        then:
            testOutcomes.durationInSeconds == 1.78
    }

    def "should provide total test duration in seconds for a set of tests when the time is zero"() {
        when:
            def testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/test-outcomes/with-no-steps"));
        then:
            testOutcomes.durationInSeconds == 0.0
    }

    def "should count tests in set"() {
        when:
            def testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.XML).from(directoryInClasspathCalled("/tagged-test-outcomes"));
        then:
            testOutcomes.total == 3
    }


    def "should count the number of scenarios correctly"() {
        when:
            def testOutcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.JSON).from(directoryInClasspathCalled("/sample-full-json-report"));
        then:
            testOutcomes.totalTestScenarios == 11
        and:
            testOutcomes.total == 23
        and:
            testOutcomes.totalTests.withResult(TestResult.SUCCESS) == 4
        and:
            testOutcomes.totalTests.withResult(TestResult.PENDING) == 19
    }

}
