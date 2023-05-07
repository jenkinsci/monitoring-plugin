Monitoring scripts
==================

Several scripts to display data about http sessions, threads, memory,
JVM or MBeans, when using the [Monitoring plugin](https://plugins.jenkins.io/monitoring).

# Jenkins Script Console

Jenkins features a nice Groovy script console which allows to run
arbitrary scripts on the Jenkins server (or on slave nodes). This
feature can be accessed from the "manage Jenkins" link, typically at
your <http://server/jenkins/script>. See [more information and scripts](https://wiki.jenkins.io/display/JENKINS/Jenkins+Script+Console).

# Monitoring Scripts and Alerts for the Jenkins instance

If the [Monitoring plugin](https://plugins.jenkins.io/monitoring) is installed, you can also use the following
monitoring scripts in the **Jenkins script console**. To run a script
periodically, you could also create and schedule a job to execute a
**system groovy script** with the [groovy plugin](https://wiki.jenkins.io/display/JENKINS/Groovy+plugin). (A system Groovy script
executes insides Jenkins instance's JVM, but see below for specific
scripts for Jenkins slaves.)

The data printed by the scripts below can be displayed by the reports of
the [Monitoring plugin](https://plugins.jenkins.io/monitoring), but the scripts can be used and customized to
display some particular data or to automate some action.

#### Execute GC

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
System.gc();
after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
println I18N.getFormattedString("ramasse_miette_execute", Math.round((before - after) / 1024));
```

#### Display HTTP sessions

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
println SessionListener.getSessionCount() + " sessions:";
sessions = SessionListener.getAllSessionsInformations();
for (session in sessions) {
    println session;
}
```

#### Display a simple threads dump

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
java = new JavaInformations(Parameters.getServletContext(), true);
threads = java.getThreadInformationsList();
println threads.size() + " threads (" + java.activeThreadCount + " http threads active):";
for (thread in threads) {
    println "";
    println thread;
    for (s in thread.getStackTrace())
        println "    " + s;
}
```

#### Display deadlocked threads

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
java = new JavaInformations(Parameters.getServletContext(), true);
threads = java.getThreadInformationsList();
deadlocked = new java.util.ArrayList();
for (thread in threads) {
  if (thread.deadlocked)
    deadlocked.add(thread);
}
println deadlocked.size() + " deadlocked threads / " + threads.size() + " threads (" + java.activeThreadCount + " http threads active)";
for (thread in deadlocked) {
  println "";
  println thread;
  for (s in thread.getStackTrace()) 
    println " " + s;
}
```

#### Display some memory data

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
memory = new MemoryInformations();
println "\nused memory:\n    " + Math.round(memory.usedMemory / 1024 / 1024) + " Mb";
println "\nmax memory:\n    " + Math.round(memory.maxMemory / 1024 / 1024) + " Mb";
println "\nused perm gen:\n    " + Math.round(memory.usedPermGen / 1024 / 1024) + " Mb";
println "\nmax perm gen:\n    " + Math.round(memory.maxPermGen / 1024 / 1024) + " Mb";
println "\nused non heap:\n    " + Math.round(memory.usedNonHeapMemory / 1024 / 1024) + " Mb";
println "\nused physical memory:\n    " + Math.round(memory.usedPhysicalMemorySize / 1024 / 1024) + " Mb";
println "\nused swap space:\n    " + Math.round(memory.usedSwapSpaceSize / 1024 / 1024) + " Mb";
```

#### Display some JVM data

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
java = new JavaInformations(Parameters.getServletContext(), true);
println "\nsessions count:\n    " + java.sessionCount;
println "\nactive HTTP threads count:\n    " + java.activeThreadCount;
println "\nthreads count:\n    " + java.threadCount;
println "\nsystem load average:\n    " + java.systemLoadAverage;
println "\nsystem cpu load:\n " + java.systemCpuLoad;
println "\navailable processors:\n    " + java.availableProcessors;
println "\nhost:\n    " + java.host;
println "\nos:\n    " + java.os;
println "\njava version:\n    " + java.javaVersion;
println "\njvm version:\n    " + java.jvmVersion;
println "\npid:\n    " + java.pid;
println "\nserver info:\n    " + java.serverInfo;
println "\ncontext path:\n    " + java.contextPath;
println "\nstart date:\n    " + java.startDate;
println "\nfree disk space in Jenkins directory:\n " + Math.round(java.freeDiskSpaceInTemp / 1024 / 1024) + " Mb";
```

#### Display heap histogram (object instances per class)

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
classes = VirtualMachine.createHeapHistogram().getHeapHistogram();
println "class    instances    bytes    source";
println "=====================================";
for (c in classes) {    
  println c.name + "    " + c.instancesCount + "    " + c.bytes + "    " + c.source;
}
```

#### Take a heap dump

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
if (System.getProperty("java.vendor").contains("IBM")) {
  Action.HEAP_DUMP.ibmHeapDump();
  println I18N.getString("heap_dump_genere_ibm");
} else {
  heapDumpPath = Action.HEAP_DUMP.heapDump().getPath();
  println I18N.getFormattedString("heap_dump_genere", heapDumpPath);
}
```

#### Display some MBean attribute value

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
exampleAttribute = "java.lang:type=OperatingSystem.ProcessCpuTime";
println exampleAttribute + " = " + MBeans.getConvertedAttributes(exampleAttribute);
```

#### Display stats of builds and build steps having mean time greater than severe threshold

(By default, severe threshold = 2 x stddev of all durations and warning threshold = 1 x stddev)

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
buildCounter = CounterRunListener.getBuildCounter();
aggreg = new CounterRequestAggregation(buildCounter);
for (request in aggreg.getRequests()) {
  if (request.getMean() >= aggreg.getSevereThreshold() || request.getCpuTimeMean() >= aggreg.getSevereThreshold()) {
    println(request.getName()
      + ", hits=" + request.getHits()
      + ", mean=" + request.getMean()
      + ", max=" + request.getMaximum()
      + ", stddev=" + request.getStandardDeviation()
      + ", cpuTimeMean=" + request.getCpuTimeMean()
      + ", systemErrorPercentage=" + request.getSystemErrorPercentage());
  }
}
```

#### Alerts

You can send alerts with a Jenkins job, using a **system groovy script** with the [groovy plugin](https://plugins.jenkins.io/groovy).

Suppose that you want to check every 15 minutes on the Jenkins instance, if the system load average is above 50 or if the active HTTP threads
count is above 100 or if there are deadlocked threads or if there are less than 10 Gb free disk space left:

* Create a freestyle job in Jenkins by clicking "New item".
* Check "Build periodically" and write a schedule, "*/15 * * * *" for example.
* Add a build step "Execute system Groovy script" and write a script:

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
java = new JavaInformations(Parameters.getServletContext(), true);
memory = java.memoryInformations;
println "used memory = " + Math.round(memory.usedMemory / 1024 / 1024) + " Mb";
println "active HTTP threads count = " + java.activeThreadCount;
println "system load average = " + java.systemLoadAverage;
println "free disk space in Jenkins directory = " + Math.round(java.freeDiskSpaceInTemp / 1024 / 1024) + " Mb";
threads = java.getThreadInformationsList();
deadlocked = new java.util.ArrayList();
for (thread in threads) {
  if (thread.deadlocked)
    deadlocked.add(thread);
}
println deadlocked.size() + " deadlocked threads / " + threads.size() + " threads";
for (thread in deadlocked) {
  println "";
  println thread;
  for (s in thread.getStackTrace())
    println " " + s;
}
if (java.systemLoadAverage > 50) throw new Exception("Alert for Jenkins: systemLoadAverage is " + java.systemLoadAverage);
if (java.activeThreadCount > 100) throw new Exception("Alert for Jenkins: activeThreadCount is " + java.activeThreadCount);
if (deadlocked.size() > 0) throw new Exception("Alert for Jenkins: " + deadlocked.size() + " deadlocked threads");
if (java.freeDiskSpaceInTemp / 1024 / 1024 < 10000) throw new Exception("Alert for Jenkins: only " + Math.round(java.freeDiskSpaceInTemp / 1024 / 1024) + " Mb free disk space left");
```

Or any script with monitoring values in this page.

* Add a post-build action "E-mail Notification" and write your email in "Recipients".
* You can also configure "Discard old builds" and write a description.
* Save.
* Click "Build now" to test it.

# Monitoring Scripts for Jenkins slaves

In the following scripts, `new RemoteCallHelper(null)` can be used to get data for all online slaves, or `new RemoteCallHelper("my-slave")` for a particular slave named "my-slave".

#### Display jvm data, memory data, deadlocked threads by node, stack-traces of threads

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
String nodeName = null; // null for all nodes, not null for a particular node
Map mapByNodeName = new RemoteCallHelper(nodeName).collectJavaInformationsListByName();
for (node in mapByNodeName.keySet()) {
  java = mapByNodeName.get(node);
  println "\nNode:\n " + node;
  println "\nsessions count:\n " + java.sessionCount;
  println "\nactive HTTP threads count:\n " + java.activeThreadCount;
  println "\nthreads count:\n " + java.threadCount;
  println "\nsystem load average:\n " + java.systemLoadAverage;
  println "\nsystem cpu load:\n " + java.systemCpuLoad;
  println "\navailable processors:\n " + java.availableProcessors;
  println "\nhost:\n " + java.host;
  println "\nos:\n " + java.os;
  println "\njava version:\n " + java.javaVersion;
  println "\njvm version:\n " + java.jvmVersion;
  println "\npid:\n " + java.pid;
  println "\nserver info:\n " + java.serverInfo;
  println "\ncontext path:\n " + java.contextPath;
  println "\nstart date:\n " + java.startDate;
  println "";
  memory = java.memoryInformations;
  println "\nused memory:\n " + Math.round(memory.usedMemory / 1024 / 1024) + " Mb";
  println "\nmax memory:\n " + Math.round(memory.maxMemory / 1024 / 1024) + " Mb";
  println "\nused perm gen:\n " + Math.round(memory.usedPermGen / 1024 / 1024) + " Mb";
  println "\nmax perm gen:\n " + Math.round(memory.maxPermGen / 1024 / 1024) + " Mb";
  println "\nused non heap:\n " + Math.round(memory.usedNonHeapMemory / 1024 / 1024) + " Mb";
  println "\nused physical memory:\n " + Math.round(memory.usedPhysicalMemorySize / 1024 / 1024) + " Mb";
  println "\nused swap space:\n " + Math.round(memory.usedSwapSpaceSize / 1024 / 1024) + " Mb";
  println "";
  threads = java.getThreadInformationsList();
  deadlocked = new java.util.ArrayList();
  for (thread in threads) {
    if (thread.deadlocked)
      deadlocked.add(thread);
  }
  println deadlocked.size() + " deadlocked threads / " + threads.size() + " threads (" + java.activeThreadCount + " threads active)";
  for (thread in deadlocked) {
    println "";
    println thread;
    for (s in thread.getStackTrace())
      println " " + s;
  }
  println "";
  println "*************************************************************";
  println "";
}
```

#### Display some MBean attributes values by node

```groovy
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;
import net.bull.javamelody.internal.common.*;
String exampleAttributes = "java.lang:type=OperatingSystem.ProcessCpuTime|java.lang:type=Memory.HeapMemoryUsage";
String nodeName = null; // null for all nodes, not null for a particular node
List values = new RemoteCallHelper(nodeName).collectJmxValues(exampleAttributes);
for (String value in values) {
  println exampleAttributes + " = " + value;
}
```
