![Logo](/docs/images/systemmonitor32.png) Monitoring plugin
=================

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/monitoring.svg)](https://plugins.jenkins.io/monitoring)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/monitoring.svg?color=blue)](https://plugins.jenkins.io/monitoring)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/monitoring-plugin/master)](https://ci.jenkins.io/job/Plugins/job/monitoring-plugin)
[![JIRA](https://img.shields.io/badge/issue_tracker-JIRA-red.svg)](https://issues.jenkins-ci.org/issues/?jql=component%20%3D%20monitoring-plugin)

[Monitoring plugin](https://plugins.jenkins.io/monitoring): Monitoring of the performance of Jenkins itself with [JavaMelody](https://github.com/javamelody/javamelody/wiki).

Open the [report](http://localhost:8080/monitoring) (or <http://yourhost/monitoring>) after installation.

Author : Emeric Vernat (evernat at free.fr)

[License ASL](http://www.apache.org/licenses/LICENSE-2.0)

## Features summarized

* Charts of memory, cpu, system load average, http response times by day, week, month, year or custom period
* Statistics of http requests with mean response times, mean cpu times, mean response size by request and by day, week, month, year or custom period
* Errors and logs
* Current http requests
* Threads
* Heap histogram (instances and sizes by class)
* Http sessions
* Process list of OS
* MBeans
* Actions for GC, heap dump and invalidate session(s)
* Report in html or pdf
* In English, German, French, Portuguese, Italian, Czech, Ukrainian or Chinese
* Jenkins security
* For Jenkins slaves:
    * The [report](http://localhost:8080/monitoring/nodes) for the
        nodes is available at <http://yourhost/monitoring/nodes>
    * Charts aggregated for all nodes of memory, cpu, system load
        average, number of running builds, build queue length, build
        times by period
    * Detailed statistics of the build times and of the build steps by period
    * Threads, process list and MBeans for each nodes
    * Heap histogram aggregated for all nodes
* For each individual node (each node in <http://yourhost/computer>),
    reports and actions are available from the "Monitoring" page in the
    contextual menu or in the detail of the node:
    * Threads, process list, MBeans of that node only
    * Heap histogram of that node
    * Actions for GC, heap dump
* And more...

![Graphics](/docs/images/graphics.png)
![Statistics](/docs/images/statistics.png)
![System infos and threads](/docs/images/system_infos_and_threads.png)

[Online help of JavaMelody](https://github.com/javamelody/javamelody/wiki/resources/Online_help_of_the_monitoring.pdf)

Some [Monitoring scripts](https://github.com/jenkinsci/monitoring-plugin/blob/master/docs/MonitoringScripts.md) can be executed using the Jenkins Script Console.
The "Monitoring" plugin can be installed by point and click in the plugin manager of a Jenkins server, or it can be downloaded from <http://mirrors.jenkins-ci.org/plugins/monitoring/>.

You can contribute translations on [this website](https://poeditor.com/join/project/QIaCp4bThS).

## Release notes

#### Since 1.85.0

See [Releases](https://plugins.jenkins.io/monitoring/#releases)

#### 1.84.0

Canceled

#### 1.83.0 (May 5, 2020)

 * improved: The javamelody parameter `http-transform-pattern` is already used in the plugin, in order to limit disk IO and disk space usage for the statistics and RRD graphs of http requests. If that's not enough, some RRD files of graphs for http requests will now be automatically deleted everyday at midnight to limit the disk space used by RRD files under 20 MB. You may configure that limit with the javamelody parameter `max-rrd-disk-usage-mb` (20 by default). And old statistics and graphs are automatically deleted like before. ([553b323](https://github.com/javamelody/javamelody/commit/553b323))
 * added: As an external API, dump of graphs values as XML or as TXT, for the choosen period or for all periods ([d11e6a8](https://github.com/javamelody/javamelody/commit/d11e6a8)). See [doc](https://github.com/javamelody/javamelody/wiki/ExternalAPI#dump-graph-as-xml-or-txt).
 * added: Links to Last value, Dump XML, Dump TXT below the zoomed graphics ([3967ea2](https://github.com/javamelody/javamelody/commit/3967ea2)).
 * added: if using Tomcat, display sources of Tomcat's classes from stack-traces like for the webapp's dependencies ([9907e93](https://github.com/javamelody/javamelody/commit/9907e93)).

#### 1.82.0 (Mar 1, 2020)

 * improved: in the http sessions, identify the new MS Edge browser as Edg instead of Chrome ([304819f](https://github.com/javamelody/javamelody/commit/304819f)).
 * optimized: when displaying the report, lazy load images in "Other charts" ([cb18db5](https://github.com/javamelody/javamelody/commit/cb18db5)).
 * clarify that statistics in reports starts at midnight (in the server's time zone), for example "1 day since midnight" ([#897](https://github.com/javamelody/javamelody/issues/897)).

#### 1.81.0 (Dec 29, 2019)

* fix [JENKINS-60433](https://issues.jenkins-ci.org/browse/JENKINS-60433): JEP-200 error on HsErrPid.
* fix [#871](https://github.com/javamelody/javamelody/issues/871), thanks to Vicente Rossello Jaume: In the [optional collect server](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#optional-centralization-server-setup), each current request already displayed once is not displayed anymore after refresh.
* Look further to display the remote user in the current requests. And add sessionId attribute in the current requests for the [External API](https://github.com/javamelody/javamelody/wiki/ExternalAPI). ([PR 873](https://github.com/javamelody/javamelody/pull/873), thanks to Eugene Kortov)
* fix [#884](../issues/884), CircularReferenceException in [External API](ExternalAPI) with JSON for current requests.
* added: graph of Usable disk space next to Free disk space in "Other charts" (the usable disk space may be lower than the free disk space, [#875](https://github.com/javamelody/javamelody/issues/875)).
* added: if the monitoring of a Jenkins server is added in the [optional collect server](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#optional-centralization-server-setup), the monitoring of Jenkins nodes including builds are automatically added at the same time ([ee06c01](https://github.com/javamelody/javamelody/commit/ee06c01)).

#### 1.80.0 (Nov 3, 2019)

* After the [JavaMelody real case 1](RealCase1), the [real case 2](RealCase2) is an the investigation of a Java application with some very slow screens, with detailed explanation on monitoring metrics, statistics and found issues. And the [real case 3](RealCase3) on the investigation of why dozen of users are working extra-hours and of other issues. [#JavaMelodyRealCase](https://twitter.com/search?q=%23JavaMelodyRealCase)
* added a graph in /monitoring/nodes of the sum of waiting durations in seconds of the builds in the build queue, next to the graph of the build queue length
    ([efa4d03](https://github.com/jenkinsci/monitoring-plugin/commit/efa4d0317122ed629190d7206a32a462a0228b5f))
* moved documentation from [wiki](https://wiki.jenkins.io/display/JENKINS/Monitoring) to [github](https://github.com/jenkinsci/monitoring-plugin/blob/master/README.md)
* added new Prometheus metrics in .../monitoring?format=prometheus: used non-heap memory, used buffered memory, used physical memory, used swap space, loaded classes count (a704562).

#### 1.79.0 (Jul 26, 2019)

* fix [JENKINS-58419](https://issues.jenkins-ci.org/browse/JENKINS-58419):
    No redirect after login in the CAS plugin since 1.78.0.
* fix [JENKINS-58388](https://issues.jenkins-ci.org/browse/JENKINS-58388),
    broken 'report' link in Available and Updated tabs of plugin manager.
* fix [\#843](https://github.com/javamelody/javamelody/issues/843):
    when using Tomcat, Tomcat info is not available anymore since Tomcat 8.5.35 and Tomcat 9.0.13.
* fix [\#847](https://github.com/javamelody/javamelody/issues/847):
    When downloading more than 2GB, assertionError may occur.

#### 1.78.0 (Jul 2, 2019)

* Improved the rendering of the management links in the Administer page
    ([JENKINS-57373](https://issues.jenkins-ci.org/browse/JENKINS-57373)).  
    Note that the link on "Monitoring of memory, cpu, http requests and
    more in Jenkins master." goes to the "/monitoring" page.  
    And the link on "You can also view the monitoring of builds, build
    queue and Jenkins nodes." goes to the "/monitoring/nodes" page.
* added: display an alert at the top of the monitoring page when there
    is an exception while collecting data, in order to make easier to
    fix basic technical issues in javamelody
    ([a7a8b26](https://github.com/javamelody/javamelody/commit/a7a8b26)).
    For example, `IOException: No space left on device`.
* fix to still flush the response when no content in
    FilterServletResponseWrapper.flushBuffer()
    ([\#836](https://github.com/javamelody/javamelody/issues/836))
* added: Czech translations
    ([2d85a88](https://github.com/javamelody/javamelody/commit/2d85a88),
    thanks to *Lukáš Karabec*)
* improved: missing German translations
    ([c19539b](https://github.com/javamelody/javamelody/commit/c19539b),
    thanks to *Michael Dobrovnik*)
* To contribute in your own language, join the translation project at
    <https://poeditor.com/join/project/QIaCp4bThS>.

#### 1.77.0 (Apr 21, 2019)

* improved: better aggregation of http requests. The javamelody parameter used by default in this plugin is now
    `-Djavamelody.http-transform-pattern=/\d+/|(?<=/static/|/adjuncts/|/bound/)[\w\-]+|(?<=/ws/|/user/|/testReport/|/javadoc/|/site/|/violations/file/|/cobertura/).+|(?<=/job/).+(?=/descriptorByName/)`.
* added: Italian translations
    ([ffc028f](https://github.com/javamelody/javamelody/commit/ffc028f),
    thanks to *Gianluca Maiorino*)
* added: Ukrainian translations
    ([073bc6d](https://github.com/javamelody/javamelody/commit/073bc6d),
    thanks to *Yevgen Lasman*)
* To contribute in your own language, join the translation project at
    <https://poeditor.com/join/project/QIaCp4bThS>.
* added: ability to upload heap dump files to [AWS
    S3](https://aws.amazon.com/s3/) ([PR
    810](https://github.com/javamelody/javamelody/pull/810), thanks to
    *Salah Qasem*).  
    To enable the upload of heap dump files to [AWS
    S3](https://aws.amazon.com/s3/):
    1.  add a parameter `heap-dump-s3-bucketname` with the S3 bucket
        name, in system properties. For example
        `-Djavamelody.heap-dump-s3-bucketname=mybucket` in your
        jenkins.xml file. And restart.
    2.  Download this [aws-s3-library
        plugin](https://github.com/javamelody/aws-s3-library/releases/download/1.11.136/aws-s3-library.hpi)
        and install it with the Advanced tab of the Plugin manager in
        Jenkins.
    3.  You also need to provide AWS credentials and AWS region as AWS
        as environnement variables or system properties or credentials
        or config files or Amazon services, see
        [doc](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#upload-heap-dumps-to-aws-s3)
        (scroll down if the target page does not scroll to the right
        chapter).

#### 1.76.0 (Jan 27, 2019)

* You can now contribute translations for javamelody by using a
    dedicated website at POEditor. You may contribute some
    "untranslated" labels for German and Portuguese or you may
    contribute new translations for Spanish, Italian or your own
    language. Join the translation project at
    <https://poeditor.com/join/project/QIaCp4bThS>
* fix for Prometheus integration: exclude metrics which have no sense
    (javamelody\_log\_duration\_millis, javamelody\_log\_errors\_count,
    javamelody\_error\_errors\_count) and metrics for statistics which
    are not displayed in the reports
    ([e1db7c5](https://github.com/javamelody/javamelody/commit/e1db7c5),
    [c0f34a2](https://github.com/javamelody/javamelody/commit/c0f34a2))
* fix [\#806](https://github.com/javamelody/javamelody/issues/806) for
    Prometheus integration again: it was printed '\<?\>' instead of NaN,
    for 'lastValue' on Java 8 and before.

#### 1.75.0 (Dec 9, 2018)

* Fix [\#794](https://github.com/javamelody/javamelody/issues/794) Compatibility with Google App Engine using Java 8.
* Fix [\#779](https://github.com/javamelody/javamelody/issues/779) When using JSVC to launch Tomcat, InternalError: errno: 13 error: Unable to open directory /proc/self/fd
* Enhanced: added X-Frame-Options: SAMEORIGIN in the reports.
* CSRF protection is automatically enabled in the plugin, if CSRF protection is enabled in Jenkins. (Note that a restart is needed if changed in Jenkins.)

Added the Offline viewer tool for some degraded cases:

* If ever you don't have access to the online reports of javamelody on the running server,
* or if you want to view the reports but the server is no longer running,
* then the offline viewer may be for you. See [doc](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#offline-viewer)

#### 1.74.0 (Sep 4, 2018)

* It is a **recommended upgrade for security** to fix a [XML External Entity (XXE) processing](https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing) vulnerability. CVE-ID is
    [CVE-2018-15531](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2018-15531).
    Thanks to *mounsurf & huanying* for reporting the vulnerability. But note that Jenkins uses the Woodstox parser so it is currently safe from this XXE vulnerability.
* Fix warning logs about serializing anonymous classes ([issue 768](https://github.com/javamelody/javamelody/issues/768)).
* fix: do not require Log4J when sending metrics to [InfluxDB](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-influxdb)
    or [Datadog](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-datadog).
* added: native calendar widget to choose dates for a custom period
    ([84a1d63](https://github.com/javamelody/javamelody/commit/84a1d63321449f71dec6069d3e712ee67e3ef5d6),
    with help from my colleague Fabien at [KleeGroup](http://www.kleegroup.com))

#### 1.73.1 (Jun 27, 2018)

* Compatibility with JDK 9: `-Djdk.attach.allowAttachSelf=true` is no
    longer required to have heap histogram and fix the display of source
    for a JDK class.

#### 1.73.0 (Jun 20, 2018)

* Compatibility with JDK 9: fix heap dump and heap histogram, display
    again the graphs of cpu, opened files and physical memory. ~~Note:
    to have the memory histogram in JDK 9 or later, add
    `-Djdk.attach.allowAttachSelf=true` in the java command line.~~
* Fix "no token found" with Prometheus integration for a Windows
    server
    ([e47b11b](https://github.com/javamelody/javamelody/commit/e47b11bbaf0772151a7cae8ff97227516cd86b04))
* Enhancement: After generating a heap dump, zip it to reduce its size
    ([98cb8bc](https://github.com/javamelody/javamelody/commit/98cb8bc08103f2ff00a4f5e72be64ddf278f2656))
* Added: The mean number of allocated Kilobytes per request is now
    displayed in the stats, next to the mean cpu per request. It is the
    memory used in the heap per request and which will have to be
    cleaned by the Garbage Collector later. And when more memory is used
    per request, more cpu will be used by the GC. As an example, 1 Gb
    allocated in a request, without a good reason, is probably a
    problem.
    ([33fe61d](https://github.com/javamelody/javamelody/commit/33fe61d5af0dc46a35c1f1a0dc26952651612055))
* Added: when crash logs (hs\_err\_pid\*.log files) are found, display
    them in the system actions
    ([5ebb28e](https://github.com/javamelody/javamelody/commit/5ebb28e9bfe98cacef6c95c9d3b5f338a444efc9))
* Added: in the zoomed charts, display of the [95th
    percentile](https://en.wikipedia.org/wiki/Burstable_billing) line.
    It shows what would be the maximum on the period if 5% of the
    highest values (short peaks) were excluded
    ([9f7acba](https://github.com/javamelody/javamelody/commit/9f7acba4e1d21658be9db2f807a7864787c04e78))
* Fix rare issue: ArithmeticException: / by zero in JRobin RRD files
    ([JENKINS-51590](https://issues.jenkins-ci.org/browse/JENKINS-51590)).
* Removed the `prometheus-include-last-value` javamelody parameter and
    replaced it by the `includeLastValue` http parameter
    ([08aacb2](https://github.com/javamelody/javamelody/commit/08aacb2fdc122a383ad4590c1426b8e129552480),
    see
    [doc](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#exposing-metrics-to-prometheus)).
    So, in the improbable case that you used
    `prometheus-include-last-value` for Prometheus integration, change
    your scrape\_config to:

        params:
          format: ['prometheus']
          includeLastValue: ['true']

#### 1.72.0 (Apr 4, 2018)

* Fix [\#735](https://github.com/javamelody/javamelody/issues/735): NPE when there are no executors.
* Fix [\#737](https://github.com/javamelody/javamelody/issues/737) for StatsD integration.
* Fix [\#731](https://github.com/javamelody/javamelody/issues/731):
    When using [CloudWatch](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-aws-cloudwatch),
    Cloudwatch metric upload can't handle more then 20 items (with help from *marcrelation*).
* Fix [\#668](https://github.com/javamelody/javamelody/issues/668):
    `RrdException: Invalid timestamps specified` in some particular case when using custom period.

#### 1.71.0 (Feb 5, 2018)

* Added JEP-200 exclusions when using monitoring with slaves (PR
    [\#6](https://github.com/jenkinsci/monitoring-plugin/pull/6) thanks to Jesse Glick)
* Note that Jenkins core includes the monitoring classes in its
    whitelist for the monitoring plugin 1.68.0 or later, but Jenkins
    servers using the monitoring plugin 1.67 or older need to upgrade
    the plugin
    ([JENKINS-50280](https://issues.jenkins-ci.org/browse/JENKINS-50280)).
* Fix [\#700](https://github.com/javamelody/javamelody/issues/700) for
    Prometheus integration (with help from *Stefan Penndorf*).
* Fix [\#701](https://github.com/javamelody/javamelody/issues/701) for
    Datadog integration (PR
    [\#702](https://github.com/javamelody/javamelody/pull/702), thanks
    to *bluegaspode*).
* Fix [\#718](https://github.com/javamelody/javamelody/issues/718):
    NPE when displaying the webapp dependencies in some particular case.
* improved: Warn in the reports if multiple instances use the same
    storage directory
    ([\#692](https://github.com/javamelody/javamelody/issues/692))
* improved: If the `application-name` parameter is defined, use it
    when publishing metrics to InfluxDB, Graphite, StatsD, CloudWatch or
    Datadog instead of the context path
    ([\#694](https://github.com/javamelody/javamelody/issues/694))
* added: When using the [reports by
    mail](https://github.com/javamelody/javamelody/wiki/UserGuide#14-weekly-daily-or-monthly-reports-by-mail),
    a new javamelody parameter `mail-subject-prefix` can be used to
    configure the subject of the mail notification. For example, in a
    Tomcat context file:
    `<Parameter  name='javamelody.mail-subject-prefix' value='Production environment  JavaMelody reports for {0}' override='false'/>`
    (PR [\#710](https://github.com/javamelody/javamelody/pull/710),
    thanks to *vkpandey82*)
* added: In the [External
    API](https://github.com/javamelody/javamelody/wiki/ExternalAPI), the
    url `monitoring?part=lastValue&format=json` now returns all the last
    values by names. (The url
    `monitoring?part=lastValue&graph=usedMemory` already returns the
    last value of a single graph by name.)

#### 1.70.0 (Oct 29, 2017)

* added: integration with **Prometheus**: Metrics are already
    displayed in the monitoring reports. You can also scrape the same
    metrics from [Prometheus](https://prometheus.io/) at the frequency
    you wish for advanced visualizations, if you have a Prometheus
    server installed (PR
    [\#682](https://github.com/javamelody/javamelody/pull/682) & PR
    [\#684](https://github.com/javamelody/javamelody/pull/684), thanks
    to *slynn1324*). See
    [doc](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#exposing-metrics-to-prometheus).  
    Note : If Jenkins security is enabled, the system property
    `-Djavamelody.plugin-authentication-disabled=true` can be added to
    the Jenkins server in order to disable authentication of the
    monitoring page in the Monitoring plugin and to allow Prometheus to
    scrape metrics.
* added integration with **StatsD**: Metrics are already displayed in
    the charts of the monitoring reports. As an extra, you can also
    publish the same metrics to
    [StatsD](https://github.com/etsy/statsd), if you have a StatsD
    daemon installed. To enable sending the metrics, add a parameter
    `statsd-address` with hostname:port of the StatsD daemon, in system
    properties. For example
    `-Djavamelody.statsd-address=11.22.33.44:8125 `in your jenkins.xml
    file, see
    [doc](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-statsd).
    ([92aeffe](https://github.com/javamelody/javamelody/commit/92aeffe800194544b9c91c1c799b36e74d4b7436))
* fix [\#681](https://github.com/javamelody/javamelody/issues/681):
    upgrade prototype.js, effects.js and slider.js

#### 1.69.1 (Sep 20, 2017)

* remove the slf4j-api dependency from the plugin to avoid potential
    conflicts with the same dependency in jenkins core.

#### 1.69.0 (Aug 27, 2017)

* In the [Jenkins
    plugin](https://wiki.jenkins-ci.org/display/JENKINS/Monitoring), fix
    NPE when using
    [CloudWatch](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-aws-cloudwatch)
    or
    [Graphite](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-graphite)
    ([3e2b872](https://github.com/javamelody/javamelody/commit/3e2b8720f90514c000519055635ad0bb5f426531)).
* In the [Jenkins
    plugin](https://wiki.jenkins-ci.org/display/JENKINS/Monitoring) with
    FreeBSD, fix
    [JENKINS-45963](https://issues.jenkins-ci.org/browse/JENKINS-45963):
    if the collect fails for one slave, continue with the others
* added integration with **InfluxDB**: Metrics are already displayed
    in the charts of the monitoring reports. Like integrations with
    [Graphite](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-graphite)
    and [AWS
    CloudWatch](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-aws-cloudwatch),
    you can also publish the same metrics to
    [InfluxDB](https://www.influxdata.com/time-series-platform/) for
    advanced visualizations, if you have an InfluxDB server installed.
    The parameter is `influxdb-url`, see
    [doc](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-influxdb).
    ([f7c8503](https://github.com/javamelody/javamelody/commit/f7c850361bec907aaba39861e473092043e75cc4))
* added integration with **Datadog**: Like for InfluxDB, you can also
    publish the same metrics to [Datadog](https://www.datadoghq.com/)
    for advanced visualizations. The parameter is `datadog-api-key`, see
    [doc](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-datadog).
    ([239aa4e](https://github.com/javamelody/javamelody/commit/239aa4e4cdf3c2943caff779a2f5492772f1500f))

#### 1.68.0 (Jul 1, 2017)

* improved (Brazilian) Portuguese translation (PR
    [\#642](https://github.com/javamelody/javamelody/pull/642), thanks
    to *Sandro Giacomozzi*).
* added a **monitoring script** to display stats of builds and build
    steps having a mean time greater than the severe threshold. See [the
    script](https://github.com/jenkinsci/monitoring-plugin/blob/master/docs/MonitoringScripts.md#display-some-mbean-attribute-value).
* fix [\#638](https://github.com/javamelody/javamelody/issues/638):
    When using [scripts and
    alerts](https://github.com/javamelody/javamelody/wiki/ScriptsAndAlerts)
    to monitor an application, ClassNotFoundException in Jenkins if the
    monitored webapp is using Ehcache or Quartz jobs.
* Internal classes moved: Many internal classes, which were not public
    and all in the `net.bull.javamelody` package, are now moved to
    `net.bull.javamelody.internal.*` packages.
* added: In the list of threads, add an action to send an interrupt
    signal to a thread. The thread can test
    Thread.currentThread().isInterrupted() to stop itself.
    ([7977f2b](https://github.com/javamelody/javamelody/commit/7977f2b98020aebbc254d1e19138782bed882d73))
* added: PDF link in the threads page
    ([b356132](https://github.com/javamelody/javamelody/commit/b356132e06047fdae09fd63f5212464e7da9efe9))
* added integration with **Graphite**: Metrics are already displayed
    in the charts of the monitoring reports. As an extra, you can also
    publish the same metrics to [Graphite](https://graphiteapp.org/) for
    advanced visualizations, if you have a Graphite server installed.
    Metrics will be sent once per minute (default value of the
    resolution-seconds parameter) and Graphite will allow custom
    visualizations in itself or in [Grafana](http://grafana.org/). The
    names of metrics in Graphite are like
    `javamelody.appContext.hostName.metricName`, so you will be able to
    aggregate a metric using wildcards.  
    To enable sending the metrics, add a parameter `graphite-address`
    with hostname:port of the Graphite server, in system properties. For
    example `-Djavamelody.graphite-address=11.22.33.44:2003` in your
    jenkins.xml file.
    ([ce94787](https://github.com/javamelody/javamelody/commit/ce947870ed120589c49b2e080ea7be17f8ef07b0))
* added integration with **AWS CloudWatch**: Like for Graphite, you
    can also publish the same metrics to [AWS
    CloudWatch](https://aws.amazon.com/cloudwatch/) for custom
    visualizations and mail or autoscaling alarms, if you have AWS EC2
    server instance(s) with [detailed
    monitoring](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-cloudwatch-new.html)
    in AWS CloudWatch. Metrics will be sent once per minute (default
    value of the resolution-seconds parameter) and CloudWatch will allow
    [custom visualizations and mail or auto-scaling
    alarms](http://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_concepts.html).
    The names of metrics in CloudWatch are like `javamelody.metricName`.
    Metrics also include `application` and `hostname` as dimensions, so
    you will be able to filter metrics based on those dimensions. Note
    that there is a
    [pricing](https://aws.amazon.com/cloudwatch/pricing/) for CloudWatch
    metrics (it is supposed that about 25 custom metrics should mean
    about $10 per month per EC2 instance).
    ([e65d605](https://github.com/javamelody/javamelody/commit/e65d6054acec4b1126a212b9ec4605c020c7186a))  
    To enable sending the metrics:
    1.  add a parameter `cloudwatch-namespace` with the CloudWatch
        namespace, in system properties. For example
        `-Djavamelody.cloudwatch-namespace=MyCompany/MyAppDomain` in
        your jenkins.xml file (the namespaces starting with `AWS/` are
        reserved for AWS products). And restart.
    2.  Download this [aws-cloudwatch-library
        plugin](https://github.com/javamelody/aws-cloudwatch-library/releases/download/1.11.136/aws-cloudwatch-library.hpi)
        and install it with the Advanced tab of the Plugin manager in
        Jenkins.
    3.  You also need to provide AWS credentials and AWS region as AWS
        as environnement variables or system properties or credentials
        or config files or Amazon services, see
        [doc](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#sending-metrics-to-aws-cloudwatch).

#### 1.67.0 (May 12, 2017)

* fix
    [JENKINS-44241](https://issues.jenkins-ci.org/browse/JENKINS-44241):
    **[Real User Monitoring
    (RUM)](https://en.wikipedia.org/wiki/Real_user_monitoring)** in
    v1.66.0 does not work when enabled, for URLs ending with '/'.

#### 1.66.0 (May 12, 2017)

* fix [\#617](https://github.com/javamelody/javamelody/issues/617):
    Charts of http/sql stats can not be viewed after restarting the
    application (with help from *goldyliang*).
* added: Display the number of http sessions and sessions mean size
    before the list of sessions, when there are more than 20 (PR
    [\#629](https://github.com/javamelody/javamelody/pull/629), thanks
    to *Aleksandr Mashchenko*).
* improved: When displaying Java sources from stack-traces, locate
    more sources by reading Maven pom.xml of Jenkins and of the other
    dependencies.
* added: Display **webapp dependencies** in a new page, accessed by a
    link in the System informations details, and include detailed name,
    website url, Maven
    [groupId:artifactId:version](http://groupIdartifactIdversion) and
    license of each dependency, based on Maven pom.xml files.
    ([e7607c1](https://github.com/javamelody/javamelody/commit/e7607c19087d6b592c92c38b50b5e2fd1d9fd306))
* added: The version of Jenkins is displayed at the top of the report
    and a drop-down list of versions next to "Customized" period allows
    to display the report for the period of each version deployed, to
    compare between them for example.
    ([d61e65f](https://github.com/javamelody/javamelody/commit/d61e65f82c936e5dc986e4f850d4a5998b9caa2d),
    based on an idea by *dhartford*).
* added: **[Real User Monitoring
    (RUM)](https://en.wikipedia.org/wiki/Real_user_monitoring)**. It
    allows to measure the experience from the end user perspective, by
    monitoring times in the browser for each html page until it is
    displayed and ready to be used
    ([b85652a](https://github.com/javamelody/javamelody/commit/b85652a182d05517ad8151f4101d584ecc007517)).
* The Real User Monitoring works by injecting on the fly a
    [Boomerang](https://soasta.github.io/boomerang/doc/) javascript at
    the end of html pages, just before the `</body>` end tag, which
    sends data back to the monitoring in your server. Then in the
    detailed monitoring report of http requests for html pages, times
    and percentages are displayed for Network, Server execution, DOM
    processing and Page rendering, to compare which ones contribute to a
    good or bad end user experience. The Real User Monitoring does not
    add a noticeable overhead on the server, but the javascript adds a
    simple http(s) call for each html page, which may add a small
    overhead on the client browser.
* This Real User Monitoring is **not enabled by default**. To enable
    it, add the system property "-Djavamelody.rum-enabled=true" in your
    jenkins.xml file.

#### 1.65.1 (Mar 13, 2017)

* added: configuration to list Jenkins maven public repository next to
    \~/.m2/repository and Maven central, to be able to display Jenkins
    and plugins sources from stack-traces.

#### 1.65.0 (Mar 12, 2017)

* fix
    [JENKINS-42112](https://issues.jenkins-ci.org/browse/JENKINS-42112),
    HTTP user session is reported as "anonymous" when using anything but
    AbstractPasswordBasedSecurityRealm (like Google login plugin or
    Cloudbees Operations Center).
* fix compatibility with Java 9. (PR
    [\#609](https://github.com/javamelody/javamelody/pull/609) for issue
    [\#556](https://github.com/javamelody/javamelody/issues/556), thanks
    to *James Pether Sörling*)
* improved: make easier the selection of the stack-trace text in the
    tooltips of the threads list
    ([736cf0e](https://github.com/javamelody/javamelody/commit/736cf0ed88f81e8e4c0ef92b6151cfe62119bc17))
* added: pdf report in the detail page of a request.
    ([28e2474](https://github.com/javamelody/javamelody/commit/28e24742c6a2a9838ce1aaf34acb152b031e6497))
* added: links to view java source from errors and threads
    stack-traces.
    ([e5263bf](https://github.com/javamelody/javamelody/commit/e5263bf08e36ab7be35c639ba7e2899d4c0ced5d))

&nbsp;

* Source from the JDK and source from artifacts built by Maven and
    available in Maven central can be viewed. So if your server uses a
    JRE and not a JDK, source from the JDK are not available. And note
    that many artifacts available in Maven central were not built by
    Maven, for example Tomcat libraries, so sources of those artifacts
    can't be located.
* added: if the javamelody parameter
    -Djavamelody.jmx-expose-enabled=true is set (in the jenkins.xml
    file), then javamelody mbeans are available with aggregated
    statistics data about requests. The javamelody mbeans can be read
    with the MBeans screen in 'System actions' of the monitoring report
    (<http://jenkinsserver:8080/monitoring?part=mbeans>) or using JMX
    with the JConsole of the JDK. (PR
    [\#591](https://github.com/javamelody/javamelody/pull/591) thanks to
    *Alexey Pushkin*)

&nbsp;

* doc added: [Summary of javamelody
    parameters](https://github.com/javamelody/javamelody/wiki/Parameters)
    to extend the [javamelody user's
    guide](https://github.com/javamelody/javamelody/wiki/UserGuide).

~~1.64.0~~

#### 1.63.0 (Jan 16, 2017)

* added: check for updated version of javamelody. If a new version is
    available, a message is now displayed at the top of the report to
    notify about the new version. For that, javamelody pings the server
    javamelody.org. And to better understand javamelody users, anonymous
    data such as java version and OS is sent to that server at the same
    time. An example of the data sent is:
    `uniqueId="3d117c04b914c78ddbaf14818c404c8e88c6e56f", serverInfo="jetty/9.2.z-SNAPSHOT", javamelodyVersion="1.63.0", applicationType="Jenkins", javaVersion="Java(TM) SE Runtime Environment, 1.8.0_111-b14", jvmVersion="Java HotSpot(TM) 64-Bit Server VM, 25.111-b14, mixed mode", maxMemory="1024", availableProcessors="4", os="Windows 7, Service Pack 1, amd64/64", databases="", countersUsed="http|error|log", parametersUsed="log", featuresUsed="pdf", locale="fr_FR", usersMean=1, collectorApplications=-1`.
* Usage stats based on the anonymous data will be publicly available
    at <http://javamelody.org/usage/stats> for applications using
    JavaMelody v1.63 or later (including Jenkins using the Monitoring
    plugin) and able to contact the server.
* You may disable the update check with the javamelody parameter
    "update-check-disabled=true" in system properties. If you want to,
    add the system property "-Djavamelody.update-check-disabled=true" in
    the jenkins.xml file.
* The online demo of javamelody is finally back. To see it, you can
    play a bit with [this app](http://javamelody.org/demo/) (written by
    Benjamin Durand some years ago) to be sure to have some data in http
    and sql statistics, then open the [monitoring
    page](http://javamelody.org/demo/monitoring) to explore the reports.

#### 1.62.0 (Oct 1, 2016)

* fix XSS (reported by *Omar El Mandour*)

#### 1.61.0 (Sep 12, 2016)

* fix XSS (reported by *Dallas Kaman, Praetorian Group*)

#### 1.60.0 (Jun 14, 2016)

* Fix XSS in graph page ([PR
    555](https://github.com/javamelody/javamelody/pull/555), thanks to
    *Tim Helmstedt*)
* fix
    [JENKINS-34794](https://issues.jenkins-ci.org/browse/JENKINS-34794)
    Jenkins sometimes doesn't start because of transient NPE (thanks to
    *Félix Belzunce Arcos*)
* improved:
    [JENKINS-34736](https://issues.jenkins-ci.org/browse/JENKINS-34736)
    Migrate to 2.9 parent pom (thanks to *Armando Fernández*)
* added system property "csrf-protection-enabled" to enable protection
    against
    [CSRF](https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF))
    on actions such as run GC, invalidate sessions, kill thread by id
    ... (Not enabled by default since an attacker would need to know
    about javamelody in Jenkins and about its URLs and would need to
    force an administrator to access the monitoring and even in this
    case, the attacker certainly can't make Jenkins unvailable and can't
    steal data or anything.) To enable this protection, add the system
    property "-Djavamelody.csrf-protection-enabled=true" in the
    jenkins.xml file.
* Limit error messages to 1000 characters and error stack-traces to
    50000 characters, to avoid high memory consumption when log messages
    are very long ([PR
    550](https://github.com/javamelody/javamelody/pull/550), thanks to
    *Zdenek Henek*)

#### 1.59.0 (Feb 25, 2016)

* fix username in the list of http sessions
    ([d111126](https://github.com/jenkinsci/monitoring-plugin/commit/d1111261b03f7762e903724599fbf84eaa3a2d48))
* fix [issue
    533](https://github.com/javamelody/javamelody/issues/533):
    IllegalArgumentException during metrics update
    ([JENKINS-33050](https://issues.jenkins-ci.org/browse/JENKINS-33050))
* fix [issue
    532](https://github.com/javamelody/javamelody/issues/532): do not
    flush a closed output stream.
* improved: wrap very long http queries without whitespace, in order
    to fit into the screen and to avoid horizontal scrollbar
    ([3a88a75](https://github.com/javamelody/javamelody/commit/3a88a75a9554b6496ff6b224716c6ed840bd88b1))
* added: display the client's browser and OS in the list of http
    sessions
    ([6d89f42](https://github.com/javamelody/javamelody/commit/6d89f428a7a92018089953bb1697ff25b0f66fc1))
* added: Support Log4J 2 since v2.4.1 like Log4J
    ([068139d](https://github.com/javamelody/javamelody/commit/068139d70dd65ff7574c5ec3cb15045af2f33d9f))
* added: Show used memory in dialog after manual GC ([issue
    522](https://github.com/javamelody/javamelody/issues/522))

#### 1.58.0 (Nov 26, 2015)

* fix
    [JENKINS-23442](https://issues.jenkins-ci.org/browse/JENKINS-23442),
    ClassCircularityError: java/util/logging/LogRecord
    ([0ad60da](https://github.com/jenkinsci/monitoring-plugin/commit/0ad60da0038048ca69672022e54434cb3c5c87a3))
* **Upgraded mininum Jenkins** version to 1.580.1
* replace use of deprecated Jenkins api
* fix from [PR 505](https://github.com/javamelody/javamelody/pull/505)
    and PR [507](https://github.com/javamelody/javamelody/pull/507):
    German translations (thanks to *mawulf*)
* fix [issue
    492](https://github.com/javamelody/javamelody/issues/492):
    incompatibility of the release v1.57.0 (isAsyncStarted) with servlet
    api 2.5 (when using mvn hpi:run in development).
* added: 2 new graphs are displayed in "Other charts", below the main
    charts: "% System CPU" and "Used buffered memory"
    ([5029404](https://github.com/javamelody/javamelody/commit/5029404eeb24e7b5c657453d06e9fc603fa5eb0d)).
    * **% System CPU** is the CPU usage for the whole system (not just
        the JVM), between 0 and 100%.
    * **Used buffered memory** is the memory used, either by *direct
        buffers* allocated outside of the garbage-collected heap, using
        [ByteBuffer.allocateDirect](http://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html#allocateDirect%28int%29),
        or by *mapped buffers* created by mapping a region of a file
        into memory, using
        [FileChannel.map](http://docs.oracle.com/javase/7/docs/api/java/nio/channels/FileChannel.html#map%28java.nio.channels.FileChannel.MapMode,%20long,%20long%29).

#### 1.57.0 (Aug 31, 2015)

* fix: check if async before flushing the response
    ([ee87b4b](https://github.com/javamelody/javamelody/commit/ee87b4b0be7fb11fd0d81a27923732d674b932d7)
    thanks to *Mark Thomas*)
* fix: used/max file descriptor counts are not displayed in oracle
    java 8
    ([c04ef79](https://github.com/javamelody/javamelody/commit/c04ef7925977f17566581b81909bb62780b947e9)
    thanks to *Colin Ingarfield*)
* Jenkins plugin: scripts examples updated to get data from slaves in
    [Jenkins Monitoring Scripts](https://github.com/jenkinsci/monitoring-plugin/blob/master/docs/MonitoringScripts.md), below the scripts for Jenkins master.
* improved: An [api
    page](https://github.com/javamelody/javamelody/blob/master/javamelody-core/src/main/resources/net/bull/javamelody/resource/help/api.html)
    is now available in the monitoring with links to the
    [ExternalAPI](https://github.com/javamelody/javamelody/wiki/ExternalAPI).
    The path of the page in your Jenkins is
    "monitoring?resource=help/api.html".
* added: it's now easy to write scripts to get monitoring data from a
    monitored webapp and to send alerts based on thresholds, using
    Jenkins for Continuous Monitoring. See the
    [documentation](https://github.com/javamelody/javamelody/wiki/ScriptsAndAlerts).

#### JavaMelody migration to Github

* The JavaMelody project is migrated from GoogleCode
    (<https://code.google.com/p/javamelody>) to GitHub. The new project
    home page is: <https://github.com/javamelody/javamelody/wiki>
* The monitoring plugin for Jenkins does not move and is at
    <https://github.com/jenkinsci/monitoring-plugin>

#### 1.56.0 (May 2, 2015)

* fix [issue
    477](https://github.com/javamelody/javamelody/issues/477): In the
    processes list, CPU and Command are sometimes in the wrong column
* added: MULTILINE & DOTALL flags for transform-patterns regexps
    ([issue 474](https://github.com/javamelody/javamelody/issues/474),
    thanks to *Michal Bergmann*).

#### 1.55.0 (Jan 30, 2015)

* fix [issue
    453](https://github.com/javamelody/javamelody/issues/453): Chinese
    translation for heap dump (thanks to *chuxuebao*)
* fix [issue
    436](https://github.com/javamelody/javamelody/issues/436): implement
    Servlet 3.1 new methods (JavaEE 7), in FilterServletOutputStream and
    others
* fix [issue
    455](https://github.com/javamelody/javamelody/issues/455): HTTP-401
    / WWW-Authenticate wrongly reported as HTTP error

#### 1.54.0 (Now 30, 2014)

* fix: Monitoring reports of a slave didn't work if its name contains
    a space.
* fix [issue
    440](https://github.com/javamelody/javamelody/issues/440): Not able
    to start Desktop version.

#### 1.53.1 (Oct 3, 2014)

* fix: in v1.52.0 with Tomcat, graphs of bytes received/sent and of
    Tomcat active threads were not displayed anymore in "Other charts"
    ([revision
    3908](https://code.google.com/p/javamelody/source/detail?r=3908)).
* fix [issue
    439](https://github.com/javamelody/javamelody/issues/439): Display
    Linux version in "System informations", and not "Linux unknown"
* improved: Portuguese translation ([revision
    3885](https://code.google.com/p/javamelody/source/detail?r=3885),
    thanks to *Fernando Boaglio*)
* added: new system property "javamelody.sampling-included-packages"
    for a white list in [cpu
    hotspots](https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#enable-hotspots-detection),
    instead of using the "javamelody.sampling-excluded-packages" system
    property ([issue
    424](https://github.com/javamelody/javamelody/issues/424), thanks to
    *alf.hogemark*)

#### 1.53.0 (Oct 1, 2014)

* fix security issues. See [Jenkins security
    advisory](https://wiki.jenkins-ci.org/display/SECURITY/Jenkins+Security+Advisory+2014-10-01).

#### 1.52.1 (Aug 6, 2014)

* fix: for Maven type jobs on recent Jenkins versions, builds of Maven
    modules are displayed as always running in the /monitoring/nodes
    page
* fix: the logout action in the menu did not work in the
    /monitoring/nodes page

#### 1.52.0 (Aug 3, 2014)

* **Upgraded mininum Jenkins** version to 1.509.3
* added: display of **build steps** and their durations, in the detail
    of each build statistics in the "/monitoring/nodes" page. Example:

&nbsp;

* added: The system property "javamelody.nodes-monitoring-disabled"
    can be used to disable the periodic monitoring of slaves. Either at
    startup, for example "-Djavamelody.nodes-monitoring-disabled=true"
    in the jenkins.xml file, or even at runtime. **Disabling periodic
    monitoring of slaves** can help against issues, if there are Jenkins
    slaves and if the communication with slaves is too unstable in the
    Jenkins remoting.
* improved: reduce the number of RRD files created in some conditions,
    by not creating a RRD file of mean times if a request is called only
    once ([revision
    3836](https://code.google.com/p/javamelody/source/detail?r=3836)).
    Otherwise, obsolete RRD files are automatically deleted after 3
    months like before.
* added: links "View in a new page" below the tables of threads and of
    current requests ([revision
    3839](https://code.google.com/p/javamelody/source/detail?r=3839)).
    For example when Jenkins is under load, the new pages make it easy
    to refresh at will the details of threads or of the current requests
    to see if states change or not, without refreshing all the main
    page.
* added: logout action in the menu on the right of the main report
    ([revision
    3859](https://code.google.com/p/javamelody/source/detail?r=3859))
* fixed
    [JENKINS-21357](https://issues.jenkins-ci.org/browse/JENKINS-21357)
    Node monitoring action: Use the specific computer's sidepanel
    instead of Jenkins default page. But the actions on the left
    sidepanel may be broken because of
    [JENKINS-23963](https://issues.jenkins-ci.org/browse/JENKINS-23963).

#### 1.51.0 (Jun 5, 2014)

* fix: when using java 8, cpu graph was not displayed
* Drop Java 5 support ([revision
    3795](https://code.google.com/p/javamelody/source/detail?r=3795))
* Optimized desktop UI startup time, by downloading desktop app and
    caching locally ([revision
    3762](https://code.google.com/p/javamelody/source/detail?r=3762))
* improved: in a graph detail, a checkbox can now hide maximum values
    in the graph, so that average values are better displayed when much
    lower than the maximum ([issue
    368](https://github.com/javamelody/javamelody/issues/368))
* added: PID in the heap dump file name ([revision
    3773](https://code.google.com/p/javamelody/source/detail?r=3773))

#### 1.50.0 (Mar 27, 2014)

* Fix icons and links on some Jenkins servers in the new Monitoring
    page of individual nodes
    ([JENKINS-20935](https://issues.jenkins-ci.org/browse/JENKINS-20935))
* fix issue [issue
    370](https://github.com/javamelody/javamelody/issues/370): work
    around ConcurrentModificationException during Tomcat startup (which
    is a [Tomcat
    bug](https://issues.apache.org/bugzilla/show_bug.cgi?id=56082))
* fix issue [issue
    386](https://github.com/javamelody/javamelody/issues/386):
    IllegalArgumentException: No enum const class ..., in Turkish
* improved: In the US, depending on the browser's language or on the
    javamelody parameter "locale", the paper size is now Letter in the
    US, instead of A4 like in the other countries. ([revision
    3679](https://code.google.com/p/javamelody/source/detail?r=3679),
    thanks to *Dennis*)
* improved css styles: font finally fixed to Arial/Helvetica
    ([revision
    3718](https://code.google.com/p/javamelody/source/detail?r=3718)).
* added: **Menu**. A floating button is available on the right of the
    main report to drag a menu in or out. The menu displays the list of
    chapters in the report and allows to jump easily between them
    ([revision
    3705](https://code.google.com/p/javamelody/source/detail?r=3705)).
* added: **Custom reports**. Links to custom reports can be included
    in the floating menu described above.  
    For that, add a system property named "javamelody.custom-reports".
    In the value of this system property, put the list of names of the
    custom reports separated with commas. Then for each custom report,
    add a system property with the same name and its path as value. By
    default, the following properties are already defined:  
    -Djavamelody.custom-reports=JenkinsInfo,AboutMonitoring  
    -Djavamelody.JenkinsInfo=/systemInfo  
    -Djavamelody.AboutMonitoring=<https://wiki.jenkins-ci.org/display/JENKINS/Monitoring>

#### 1.49.0 (Jan 12, 2014)

* added: For each individual node (each slave in
    <http://yourhost/computer>), reports and actions are available from
    the "Monitoring" page in the contextual menu or in the detail of the
    node:
    * Threads, process list, MBeans of that node only
    * Heap histogram of that node
    * Actions for GC, heap dump
    * ([JENKINS-20935](https://issues.jenkins-ci.org/browse/JENKINS-20935),
        with help from Oleg Nenashev)
* improved: When I call the "Invalidate http sessions" action,
    invalidate all sessions except mine (I can still invalidate my
    session individually after that)
* improved: In the list of http sessions, a bullet shows which one is
    my own session, if I have a session
* improved css styles with the come back of shadows in Firefox and
    with hovers for images, \~_(see)
    _([JavaMelody\ Demo](https://code.google.com/p/javamelody/wiki/Demo))\~
    ([revision
    3614](https://code.google.com/p/javamelody/source/detail?r=3614))

#### 1.48.0 (Nov 20, 2013)

* fix
    [JENKINS-20352](https://issues.jenkins-ci.org/browse/JENKINS-20532):
    HTTP session count is high, since Jenkins v1.535 ([revision
    3569](https://code.google.com/p/javamelody/source/detail?r=3569))
* fix [issue
    354](https://github.com/javamelody/javamelody/issues/354): With the
    java update 1.7\_u45, the Desktop app does not start

#### 1.47.0 (Sep 29, 2013)

* fix [issue
    339](https://github.com/javamelody/javamelody/issues/339): "One day"
    memory leak
* fix [issue
    346](https://github.com/javamelody/javamelody/issues/346): XSS
    through X-Forwarded-For header spoofing
* fix [issue
    232](https://github.com/javamelody/javamelody/issues/232): Use the
    path of -XX:HeapDumpPath=/tmp if defined, for the directory of heap
    dump files (otherwise use the temp directory of the server as
    before)
* added: detection of cpu hotspots can be enabled ([issue
    149](https://github.com/javamelody/javamelody/issues/149), with some
    ideas from Cédric Lime).
    * In the monitoring reports, the new Hotspots screen displays CPU
        hotspots in executed methods for all the JVM. Like javamelody,
        the overhead of hotspots is low: it is based on sampling of
        stack-traces of threads, without instrumentation. And like
        javamelody, it is made to be always active if enabled. (It is
        currently not enabled by default. It may be enabled by default
        in the future.) Two parameters can be defined as system
        properties in the jenkins.xml file:
    * "javamelody.sampling-seconds" to enable the sampling and to
        define its period. A period of 10 seconds can be recommended to
        have the lowest overhead, but then a few hours may be needed to
        have significant results for a webapp under real use. If you
        don't mind having a bit more overhead or want a result faster in
        test, you can use a value of 1 or 0.1 in second for this
        parameter.
    * "javamelody.sampling-excluded-packages" to change the list of
        the excluded packages
        ("java,sun,com.sun,javax,org.apache,org.hibernate,oracle,org.postgresql,org.eclipse"
        by default)

#### 1.46.0 (Aug 4, 2013)

* fix
    [JENKINS-17757](https://issues.jenkins-ci.org/browse/JENKINS-17757)
    IllegalStateException: Timer already cancelled from
    NodesCollector.scheduleCollectNow
* use a timeout for the monitoring of slaves, when a slave is online
    but does not respond
* fix NPE when manually purging the obsolete monitoring files of
    slaves

#### 1.45.0 (Jun 6, 2013)

* added: button to kill a thread from the current requests (issue
    [issue 302](https://github.com/javamelody/javamelody/issues/302))
* added: button to donate in the html reports, to better inform users
    of this possibility

#### 1.44.0 (Mar 30, 2013)

* fix: it is currently useless to display the sql hits for the current
    requests
* fix: better aggregation of http requests in the statistics, for URLs
    /adjuncts/... and /$stapler/bound/... (also reduces the disk space
    used to store data)
* fix [issue
    289](https://github.com/javamelody/javamelody/issues/289),
    [JENKINS-15529](https://issues.jenkins-ci.org/browse/JENKINS-15529):
    NoClassDefFoundError: com/sun/management/OperatingSystemMXBean, in
    JBoss AS 7 without configuration of modules
* page added: Some can be executed using the Jenkins Script Console.

#### 1.43.0 (Jan 27, 2013)

* fix [issue
    270](https://github.com/javamelody/javamelody/issues/270): French
    Canadians can't choose a customized period
* fix: in the current requests, use the chosen period to display the
    statistics of mean times, besides the elapsed times
* added: PDF report of the current requests
* added: optional parameter "locale" to fix the locale of the reports,
    whatever the language in the browser ([issue
    271](https://github.com/javamelody/javamelody/issues/271), thanks to
    *xiukongtiao*). You can add, for example, the system property
    -Djavamelody.locale=en\_US

#### 1.42.0 (Dec 2, 2012)

* fix [issue 262](https://github.com/javamelody/javamelody/issues/262)
    NullPointerException in MonitoringFilter.doFilter(), when the webapp
    is undeployed and after a timeout
* added: New alternative User Interface for the monitoring reports
    with a **Rich Desktop Application**. Highlights :
    * All reports and data like in the web UI
    * Exports to PDF, XML and JSON formats, even if the monitored
        application does not have the dependencies to do the same in the
        web UI
    * Tabs to view different reports
    * Columns of tables may be sorted, resized and moved
    * Exports with right-click for all tabular data to CSV, PDF, RTF,
        HTML, XML, JSON formats
    * Shortcuts, such as F5
    * Started in one-click with the "Desktop" link at the top of the
        web UI. The required libraries are downloaded automatically from
        \~_(googlecode)\~ github on the first launch.
    * Uses JavaWebStart and requires [JRE 1.7](http://java.com) on the
        client
    * This new UI may be an alternative to the web UI for advanced
        users or for exports

#### 1.41.0 (Sep 30, 2012)

* fix [issue 252](https://github.com/javamelody/javamelody/issues/252)
    Add XSS protection
* fix [issue 255](https://github.com/javamelody/javamelody/issues/255)
    exception while collecting data java.io.FileNotFoundException: Could
    not open ....rrd existent

#### 1.40.0 (Aug 26, 2012)

* fix [issue
    14050](http://issues.jenkins-ci.org/browse/JENKINS-14050), also for
    the monitoring reports of Nodes.
* added in the external API: XML and JSON exports of MBeans, and of
    JNDI tree given a JNDI context path. Documentation is
    [here](https://github.com/javamelody/javamelody/wiki/ExternalAPI#xml).
    For example:
    [MBeans](http://demo.javamelody.cloudbees.net/monitoring?format=xml&part=mbeans)
    and
    [JNDI](http://demo.javamelody.cloudbees.net/monitoring?format=json&part=jndi&path=comp).

#### 1.39.0 (Jun 21, 2012)

* fix [issue
    14050](http://issues.jenkins-ci.org/browse/JENKINS-14050), also when
    security is enabled in Jenkins: Unreadable HTML response for the
    monitoring reports. Compression of the monitoring reports is now
    disabled in the plugin and the reports will be compressed by Jenkins
    starting with v1.470.

#### 1.38.0 (Jun 17, 2012)

* fix [issue
    14050](http://issues.jenkins-ci.org/browse/JENKINS-14050), but only
    when security is not enabled in Jenkins: Unreadable HTML response
    for the monitoring reports

#### 1.37.0 (Apr 29, 2012)

* fix issue 207: Incompatibility with servlet api 2.4 (Tomcat 5.5) in
    Monitoring plugin 1.36.0
* added: Reduce the possibility of storage overload by deleting
    automatically the obsolete RRD files (which were not updated for the
    last 3 months), and which were for requests which do not exist
    anymore. Note: the size of existing RRD files is fixed for ever and
    old .ser.gz files are already automatically deleted after a year.
* added: Display the disk usage of the storage at the bottom of the
    report

#### 1.36.0 (Mar 30, 2012)

* [JavaMelody](https://github.com/javamelody/javamelody/wiki) is used
    inside the Monitoring plugin. Thanks to
    [CloudBees](http://www.cloudbees.com) Jenkins CI, [nightly
    build](https://javamelody.ci.cloudbees.com/job/javamelody/),
    [javadoc](https://javamelody.ci.cloudbees.com/job/javamelody/site/apidocs/index.html)
    and
    [sources](https://javamelody.ci.cloudbees.com/job/javamelody/site/xref/index.html)
    of JavaMelody are available.  
    \~_(And\ there\ is\ a\ public\ demo\ of\ JavaMelody.\ It\ is\ based\ on\ the\ latest\ source\ in\ trunk\ and\ is\ continuously\ deployed.\ It\ is\ currently\ not\ a\ demo\ in\ Jenkins,\ but\ instead\ in\ an\ application\ with\ GWT,\ services\ and\ SQL.)\~  
    \~_(First,\ you\ can\ play\ with\ this\ quick\ and\ dirty)
    _([GWT\ application](http://demo.javamelody.cloudbees.net/))
    _(to\ have\ some\ data\ (thanks\ Benjamin))\~  
    \~_(Then\ you\ can\ view\ the\ charts\ and\ the\ statistics\ in\ the)
    _([monitoring\ reports](http://demo.javamelody.cloudbees.net/monitoring))
    _((no\ password))\~

&nbsp;

* Other notes:
    * fix blocking issue 194: The jrobin v1.5.9 artifacts are now
        available in the Maven central repository as you can see
        [here](http://search.maven.org/). The external (and currently
        unreachable) repository in the javamelody pom is now useless and
        removed.
    * fix issue 201: Unable to render MBeans tree when some
        JMX-retrieval exception occurs  
        added: if there are deadlocked threads in the JVM, print a
        warning message with the names of the deadlocked threads at the
        top of threads dump, like in the html and pdf reports
        (revision 2678)
    * This version requires a Servlet 2.5 container (fixed in 1.37.0)

#### 1.35.0 (Feb 28, 2012)

* fix issue 178: Since v1.32, "Nb of http sessions" is 0 and "View
    http sessions" is always empty.
* added: graphic of "Build queue length" in the monitoring of nodes,
    next to the graphic of "Running builds" (done in Jenkins Hackathon,
    with help from Kohsuke Kawaguchi)
* added: optional parameter (-Djavamelody.dns-lookups-disabled=true)
    to allow disabling of dns lookups with InetAddress.getLocalHost() to
    prevent long hangs on startup/shutdown in some particular
    environments (issue 181, thanks to r6squeegee)

#### 1.34.0 (Jan 28, 2012)

* Minor bugfixes such as fonts in the PDF reports for Chinese people,
    when the first PDF report was made for non Chinese people

#### 1.33.0 (Nov 29, 2011)

* fix [issue 11293](http://issues.jenkins-ci.org/browse/JENKINS-11293)
    Monitoring plugin not installed because of NoClassDefFoundError:
    org.slf4j.ILoggerFactory on IBM J9 JVM ([rev
    40073](https://svn.jenkins-ci.org/trunk/hudson/plugins/monitoring/pom.xml))

#### 1.32.1 (Oct 14, 2011)

* fix issue 151: a Java 1.6 dependency was introduced in 1.32.0
    (NoSuchFieldError: ROOT)

#### 1.32.0 (Oct 13, 2011)

* fix issue 141: exception while collecting data java.io.IOException:
    Read failed, file xyz not mapped for I/O (after out of disk space)
* added: Chinese translation, and fix display of Chinese characters in
    the JRobin graphics and in the PDF reports (issue 150)
* added: Action "Generate a heap dump" added for IBM JDK like for
    Oracle JDK, patch by David Karlsen in the javamelody users' group
* added: Add link "Dump threads as text" below the list of threads
* added: Provide an url (monitoring?action=mail\_test) to test sending
    a pdf report by mail (issue 145).

#### 1.31.0 (Aug 14, 2011)

* fix issue 128: Clean the shutdown process
* To display the username in the list of http sessions, look at
    ACEGI\_SECURITY\_LAST\_USERNAME if getRemoteUser() was null.

#### 1.30.0 (Jul 15, 2011)

* fix issue 116: MBeans overview does not work with java 1.5
* fix issue 117: Sorting of numbers is based on String comparison in
    German
* fix issue 122: Average number of requests per minutes seems to be
    wrong, with a single day in a custom period
* fix issue 124: Start date removed from the text at the top of the
    monitoring page, except if the "All" period is selected.

#### 1.29.0

* fix issue 106: a few HTTP hits are lost in the statistics for the
    first hit(s) on new requests (no impact on statistics once requests
    are known)

#### 1.28.0

* fix issue 99: NullPointerException when displaying the list of
    process on AIX

#### 1.27.0

* added: pdf report of MBeans

#### 1.26.0

* fix [issue 8344](http://issues.jenkins-ci.org/browse/JENKINS-8344)
    (ClassNotFoundException: net.bull.javamelody.SessionListener)
* added: **Portuguese Brazil translation**, including the online help,
    thanks to *Luiz Gonzaga da Mata* and *Renan Oliveira da Cunha*. This
    translation is the default for all Portuguese people. The first
    language in the browser settings should be Portuguese (pt) or
    Portuguese/Brazil (pt-br) to use it. The [online help in Portuguese
    Brazil](https://github.com/javamelody/javamelody/blob/master/javamelody-core/src/site/resources/Ajuda_on-line_do_monitoring.pdf)
    is also available.
* added: in the nodes report, chart of the number of the running
    builds by period

#### 1.25.0

* fix some issues in the monitoring of Jenkins nodes when the
    operating systems of the nodes are heterogeneous
* fix issue 80: Memory histogram should be supported on Mac OS X
* added: if JRockit, display the JRockit specific MBeans
* added: pdf reports of http sessions and of heap histogram

#### 1.24.0

* fix issue 74: "View OS Processes" does not work on MAC OS X Server

&nbsp;

* added: In the system actions, new view "MBeans" with the values and
    the descriptions of the attributes. (MBeans contain configuration
    and low-level data on the application server and on the JVM). The
    values are viewable but not writable and operations can not be
    performed.

&nbsp;

* added: Monitoring of the **Jenkins nodes** (slaves in general)
    similar to the monitoring of the Jenkins master.  
      
    If the monitoring of the Jenkins master is available at
    <http://localhost:8080/monitoring> then the monitoring of the
    Jenkins nodes is available at
    <http://localhost:8080/monitoring/nodes>.  
      
    The monitoring of the Jenkins nodes includes:
    * Aggregated "used memory" chart, aggregated "% cpu" chart
        (between 0 and 100) for day, week, month, year or a custom
        period.
    * Chart of the build times over time for the selected period
    * Other aggregated charts: % GC, threads count, loaded classes
        count, system load average, open file descriptors count and more
        for the selected period
    * Statistics of the build times for the selected period
    * Running builds with time elapsed
    * Threads informations for each node including name, state,
        stack-trace and an action to kill any thread
    * Heap histogram aggregated for all nodes
    * Current system informations for each node
    * MBeans for each node
    * Last values in charts and MBeans values. For example, the
        following URLs can be used: 1 and 2 (the
        plugin-authentication-disabled parameter could be used)
    * Process informations for each node
    * Actions to execute the GC or to generate a heap dump on each
        node

&nbsp;

* Due to the current infrastructure changes for the [maven2-repository
    in java.net](https://maven2-repository.dev.java.net/), the
    "Monitoring" plugin 1.24.0 was not in the plugin manager.

#### 1.23.0

* fix [issue
    66](http://code.google.com/p/javamelody/issues/detail?id=66): Since
    tomcat 6.0.21 and when tomcat based authentication is used, session
    count is false and there is a possible memory leak for invalidated
    http sessions in v1.22.0. This is because of the changes for tomcat
    enhancement
    [45255](https://issues.apache.org/bugzilla/show_bug.cgi?id=45255).
    To tomcat experts: Isn't an http session supposed to die first
    before being able to born a second time?

#### 1.22.0

* fix: Maximum values in statistics could be incorrect
* added major feature: graphic of the number of sessions and details
    on http sessions with the link in "System informations" (this is
    emulated without httpsessionlistener, because httpsessionlistener
    can't be used in Jenkins plugins)
* added: If Jenkins security is enabled, the system property
    -Djavamelody.plugin-authentication-disabled=true can be added to a
    Jenkins server in order to disable authentication of the monitoring
    page in the Monitoring plugin and to be able to add the server to a
    javamelody centralized collect server. (A system property like
    -Djavamelody.allowed-addr-pattern=127\\.0\\.0\\.1 can also be added
    with the ip address of the collect server)

#### 1.21.0

* fix to display the list of http sessions when Tomcat throws an
    exception "Session already invalidated"
* fix to display the list of process when using Windows in Germany
* added: If Tomcat or JBoss, new graphics in "Other charts" for the
    number of active http and ajp threads in the server, the number of
    bytes received per minute and the number of bytes sent per minute by
    the server.

#### 1.20.0

* added: German translation thanks to Ewald Arnold. We would like to
    have [feedback
    here](http://groups.google.fr/group/javamelody/browse_thread/thread/5656e63513436b64).
* fix: In the html display of the current requests, put back the
    complete http request with query parameters and values, like it was
    displayed before v1.17.0

#### 1.19.0

* fix NumberFormatException requesting process information.
* fix (removed) jndi link in Winstone.
* added debugging logs.

#### 1.18.0

* other minor bugs fixed.

#### 1.17.0

* some minor bugs fixed.

#### 1.15.1

* change to **reduce disk usage**: Some common http requests are now
    aggregated in statistics, for example on build numbers.

The `javamelody.http-transform-pattern` parameter has now the default
value of

/\\d+/\|/site/.+\|avadoc/.+\|/ws/.+\|obertura/.+\|estReport/.+\|iolations/file/.+\|/user/.+\|/static/\\w+/

and all matches in http URLs will be replaced by "$". Note that it is
possible on each Jenkins server to change the value of this parameter
with a system property `-Djavamelody.http-transform-pattern=xxx` in the
java command line.

#### 1.15.0

* change to reduce disk usage: Graphics in http errors and in errors
    logs are no longer displayed

#### 1.14.0

* fix: There was an InternalError on ubuntu or debian using the tomcat
    package with jsvc

#### 1.13.0

* added: remember the last selected period (with a persistent cookie
    in the browser)
* added: UI option to display graphs and statistics for **custom
    periods**, via fields of start and end dates of period to display

#### 1.12.0

* added: **New charts** "Threads count", "Loaded classes count", "Used
    non heap memory", "Used physical memory", "Used swap space"
    (displayed with the new link "Other charts")
* added: Button to kill a java thread
* added: parameters can now be specified in environment variables like
    in system properties or webapp context
* added: new parameter "monitoring-path" to change the url
    "/monitoring" of the report to "/admin/monitoring" for example
* added: new parameter "mail-periods" to change the period of mail
    reports from weekly to daily or monthly or a combination of the 3
* added: display the version of JavaMelody at the bottom of the html
    and pdf reports
* added: display of "ajax GET" or "ajax POST" in http requests names
    for ajax requests

#### 1.10.0

* English: For people outside US, UK and France, default language is
    now the default locale of the server or English.

#### 1.9.0

* Fix: The "Monitoring" link in "/manage" page did not work when
    Jenkins was in a servlet context (not in the root context of the
    server).
* JavaMelody fix: someone had an exception in report on Solaris 10
    (getPID)

#### 1.8.2

* Removed jdbc graphs as there is no database in Jenkins,
* Removed sessions graph, values and links as the javamelody
    SessionListener can't be registered without modifying web.xml
* Added a link in "/manage" page
* Added checkPermission to check authentication if configured

#### 1.8.1

* Initial

## Translations

Translations for other languages such as Spanish are welcomed.

To contribute in your own language, join the translation project at
<https://poeditor.com/join/project/QIaCp4bThS>.

or see in English:
<https://github.com/javamelody/javamelody/blob/master/javamelody-core/src/main/resources/net/bull/javamelody/resource/translations.properties>  
the same in French:
<https://github.com/javamelody/javamelody/blob/master/javamelody-core/src/main/resources/net/bull/javamelody/resource/translations_fr.properties>

  

## Compiling and testing the plugin:

Use maven commands "mvn hpi:run" or "mvn package" like for all Jenkins plugins

http://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fmonitoring-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/monitoring-plugin/job/master/)
