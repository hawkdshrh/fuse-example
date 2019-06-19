# Example of JBoss A-MQ (ActiveMQ) on OpenShift using S2I

Configuration of the JBoss A-MQ image can also be modified using the S2I (Source-to-image) feature.

Custom A-MQ broker configuration can be specified by creating an openshift-activemq.xml file inside 
the git directory of your application’s Git project root. On each commit, 
the file will be copied to the conf directory in the A-MQ root and its contents used to configure the broker.

##S2I Build OpenShift:

### Create first a new project (namespace) called Broker:

```
oc new-project broker
```

Now let's s2i our conf to the jboss-amq image

```
oc new-build registry.access.redhat.com/jboss-amq-6/amq63-openshift:1.4~https://github.com/abouchama/amq63-basic-s2i.git
--> Found Docker image 82969fa (11 days old) from registry.access.redhat.com for "registry.access.redhat.com/jboss-amq-6/amq63-openshift:1.4"

    JBoss A-MQ 6.3 
    -------------- 
    A reliable messaging platform that supports standard messaging paradigms for a real-time enterprise.

    Tags: messaging, amq, java, jboss, xpaas

    * An image stream tag will be created as "amq63-openshift:1.4" that will track the source image
    * A source build using source code from https://github.com/abouchama/amq63-basic-s2i.git will be created
      * The resulting image will be pushed to image stream tag "amq63-basic-s2i:latest"
      * Every time "amq63-openshift:1.4" changes a new build will be triggered

--> Creating resources with label build=amq63-basic-s2i ...
    imagestream.image.openshift.io "amq63-openshift" created
    imagestream.image.openshift.io "amq63-basic-s2i" created
    buildconfig.build.openshift.io "amq63-basic-s2i" created
--> Success
```
To stream the build progress, run 'oc logs -f bc/amq63-basic-s2i',
You can see here that our conf openshift-activemq.xml has been copied to the image stream:

```
$ oc logs -f bc/amq63-basic-s2i
Cloning "https://github.com/abouchama/amq63-basic-s2i.git" ...
	Commit:	073c43d61cc4025ce100e02784439ff079863b97 (Update README.md)
	Author:	Gogs <gogs@fake.local>
	Date:	Tue Jan 15 19:08:27 2019 +0100
	
Using registry.access.redhat.com/jboss-amq-6/amq63-openshift@sha256:2a1fdbe0fbc5ab57bec5ff04ba114a5cb4664f68f225a205ac6451e2ba1d1c1c as the s2i builder image

Copying config files from project...
'/tmp/src/configuration/log4j.properties' -> '/opt/amq/conf/log4j.properties'
'/tmp/src/configuration/openshift-activemq.xml' -> '/opt/amq/conf/openshift-activemq.xml'
'/tmp/src/configuration/openshift-activemq.xml-default' -> '/opt/amq/conf/openshift-activemq.xml-default'
Pushing image 172.30.1.1:5000/broker/amq63-basic-s2i:latest ...
Pushed 0/5 layers, 1% complete
Pushed 1/5 layers, 21% complete
Pushed 2/5 layers, 45% complete
Pushed 3/5 layers, 81% complete
Pushed 4/5 layers, 95% complete
Pushed 5/5 layers, 100% complete
Push successful
```
Let's get now, our image stream URL:

```
$ oc get is
NAME              DOCKER REPO                              TAGS      UPDATED
amq63-basic-s2i   172.30.1.1:5000/broker/amq63-basic-s2i   latest    About a minute ago
amq63-openshift   172.30.1.1:5000/broker/amq63-openshift   1.4       6 minutes ago
```
Now, you have to change the image steam on the template "template-amq62-basic-s2i.json", like following:

```
"image": "172.30.1.1:5000/broker/amq63-basic-s2i"
```
Also, in triggers, change the name of the image stream tag in order to trigger a new deployment when it detect an Image Change:

```
"kind": "ImageStreamTag",
"namespace": "${IMAGE_STREAM_NAMESPACE}",
"name": "amq63-basic-s2i:latest"
```
In order to get the image stream tag, run the following command:

```
$ oc get istag
NAME                     DOCKER REF                                                                                                                       UPDATED          IMAGENAME
amq63-basic-s2i:latest   172.30.1.1:5000/broker/amq63-basic-s2i@sha256:e97566fedade669b45ff238e033107552ddee59c1cddff983db07172722e9713                   11 minutes ago   sha256:e97566fedade669b45ff238e033107552ddee59c1cddff983db07172722e9713
amq63-openshift:1.4      registry.access.redhat.com/jboss-amq-6/amq63-openshift@sha256:2a1fdbe0fbc5ab57bec5ff04ba114a5cb4664f68f225a205ac6451e2ba1d1c1c   18 hours ago     sha256:2a1fdbe0fbc5ab57bec5ff04ba114a5cb4664f68f225a205ac6451e2ba1d1c1c
```

###create the template in the namespace
```
$ oc create -n broker -f template-amq63-basic-s2i.json 
template.template.openshift.io/amq63-basic-s2i created
```
###create the service account "amq-service-account"
```
oc create -f https://raw.githubusercontent.com/abouchama/amq63-basic-s2i/master/amq-service-account.json
serviceaccount "amq-service-account" created
```

###ensure the service account is added to the namespace for view permissions... (for pod scaling)
```
oc policy add-role-to-user view system:serviceaccount:broker:amq-service-account
```

###use the template in the namespace then to create your Broker:
```
$ oc process amq63-basic-s2i -p APPLICATION_NAME=broker -p MQ_USERNAME=amq -p MQ_PASSWORD=topSecret -p AMQ_STORAGE_USAGE_LIMIT=1gb -p IMAGE_STREAM_NAMESPACE=broker -n broker | oc create -f -
service "broker-amq-amqp" created
service "broker-amq-mqtt" created
service "broker-amq-stomp" created
service "broker-amq-tcp" created
service "broker-amq-mesh" created
deploymentconfig "broker-amq" created
```

 Logs show:
 
 ```
 Copying Config files from S2I build
'/opt/amq/conf/broker.xml' -> '/home/jboss/broker-s2i/etc/broker.xml'
```
 
###Update of openshift-activemq.xml

You should setup the GitHub webhook URL in order to trigger a new build after each update.

You can also do it manually, like following:
```
$ git commit -am "changing my activemq conf"

$ git push -u origin master

$ oc start-build amq62-basic-s2i -n broker
build "amq63-basic-s2i-2" started

$ oc deploy broker-amq-a --latest -n broker
Started deployment #2
Use 'oc logs -f dc/broker-amq-a' to track its progress.
```
