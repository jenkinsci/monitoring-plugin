package org.jvnet.hudson.plugins.monitoring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import org.htmlunit.html.HtmlPage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class MonitoringFilterIntegrationTest {

    @Rule public JenkinsRule rule = new JenkinsRule();

    @Test
    public void test() throws Exception {
        JenkinsRule.WebClient wc = rule.createWebClient();
        HtmlPage page = wc.goTo("monitoring");
        assertThat(page.getTitleText(), startsWith("Monitoring JavaMelody"));
    }
}
