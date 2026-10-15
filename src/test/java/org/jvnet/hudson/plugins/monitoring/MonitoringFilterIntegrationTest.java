package org.jvnet.hudson.plugins.monitoring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class MonitoringFilterIntegrationTest {

    private JenkinsRule rule;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("javamelody.nodes-monitoring-disabled", "true");
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty("javamelody.nodes-monitoring-disabled");
    }

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

    @Test
    void testNodesMonitoringHandlesMissingNodeAndUninitializedCollector() throws Exception {
        NodeMonitoringAction action = new NodeMonitoringAction(rule.jenkins.toComputer(), "Monitoring", null);
        assertThat(action.getCsrfTokenUrlPart(), notNullValue());

        try (JenkinsRule.WebClient wc = rule.createWebClient()) {
            HtmlPage uninitializedNodesPart = wc.goTo("monitoring/nodes?part=processes");
            assertThat(uninitializedNodesPart.asNormalizedText(), containsString("No agents online"));

            HtmlPage nodesPage = wc.goTo("monitoring/nodes");
            assertThat(nodesPage.getTitleText(), startsWith("Monitoring JavaMelody"));

            HtmlPage missingNodePage = wc.goTo("monitoring/nodes/unknown-agent");
            assertThat(missingNodePage.asNormalizedText(), containsString("No agents online"));

            hudson.slaves.DumbSlave agent = rule.createOnlineSlave();
            HtmlPage onlineAgentPage = wc.goTo("monitoring/nodes/" + agent.getNodeName());
            assertThat(onlineAgentPage.getTitleText(), startsWith("Monitoring JavaMelody"));

            agent.toComputer().disconnect(null).get();
            HtmlPage offlineAgentPage = wc.goTo("monitoring/nodes/" + agent.getNodeName());
            assertThat(offlineAgentPage.asNormalizedText(), containsString("No agents online"));
        }
    }
}
