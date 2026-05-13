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

#### Archive

See [ReleaseNotes-old.md](ReleaseNotes-old.md)

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
