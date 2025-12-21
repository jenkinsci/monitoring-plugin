package org.jvnet.hudson.plugins.monitoring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class MonitoringFilterIntegrationTest {

    private JenkinsRule rule;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        this.rule = rule;
    }

	@Test
	void test() throws Exception {
        try (JenkinsRule.WebClient wc = rule.createWebClient()) {
            HtmlPage page = wc.goTo("monitoring");
            assertThat(page.getTitleText(), startsWith("Monitoring JavaMelody"));
        }
    }
}
