# Test AMQ Broker

Resources to Build and Deploy a [JBoss AMQ Broker](https://access.redhat.com/documentation/en-us/red_hat_jboss_a-mq/6.3/html-single/red_hat_jboss_a-mq_for_openshift/) to OpenShift for Testing

## Overview

Contents:

* [test-amq-s2i](test-amq-s2i) - Custom JBoss AMQ Broker built using [Source-To-Image (S2I)](https://docs.openshift.com/container-platform/3.11/creating_images/s2i.html)
* [config](config) - sample s2i build input for customizing `activemq.xml`
* [templates](templates) - OpenShift [templates](https://docs.openshift.com/container-platform/3.11/dev_guide/templates.html) representing OpenShift API objects to build and deploy the broker

## Building and Deploying

The following steps describe how to build and deploy the broker to OpenShift

## Create a Project

A [project](https://docs.openshift.com/container-platform/3.11/dev_guide/projects.html) in OpenShift is a collaborative workspace for deploying resources. Create a new project for the resources included in this repository:

```
oc new-project test-amq --display-name="Test AMQ" --description="Test JBoss AMQ Broker"
```

### Build the AMQ base s2i image

You'll need to create a secret so that the build pod can clone from a private repository. You may want to follow this [guide](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) to generate a GitHub developer token.

Create the secret using your GitHub credentials:

```
oc create secret generic test-amq-github --from-literal=username=<your Git Hub username> --from-literal=password=<your token> --type=kubernetes.io/basic-auth
```
Apply the build:

```
oc process -f templates/test-amq-base-build.yaml -o yaml | oc apply -f -
```
Start the build:

```
oc start-build test-jboss-amq --follow
```

### Service Account Permissions

Every pod that is deplpoyed to OpenShift is run using a [Service Account](https://docs.openshift.com/container-platform/3.6/dev_guide/service_accounts.html). To support multiple replicas of AMQ, the service account that is used to run the broker pod (default) must have _view_ rights within the project.

Execute the following command to grant _view_ rights to the default service account:

```
oc policy add-role-to-user view -z default
```

### Deploying the Broker

A template is available to deploy the customized AMQ image previously built.

Execute the following command to process the template and apply the configurations to OpenShift to deploy the AMQ broker:

```
oc process -f templates/test-amq63-persistent.yaml | oc apply -f-
```

### Extend the AMQ image with custom activemq.xml using s2i

A custom image based on the [jboss-amq-63](https://access.redhat.com/containers/#/registry.access.redhat.com/jboss-amq-6/amq63-openshift) is used to tailor the configurations for use by our test environment.

Provide the namespace where you published the Test Base s2i image earlier. We'll use that image, and add config files like `activemq.xml` to produce an image with custom configuration.

```
oc process -f templates/test-amq-s2i-build.yaml -p BASE_IMAGESTREAM_NAMESPACE=<project name> | oc apply -f-
```

With the new BuildConfig and ImageStream created, a [binary build](https://docs.openshift.com/container-platform/3.6/dev_guide/builds/build_inputs.html#binary-source) can be started using the contents in the [test-amq-s2i](test-amq-s2i) folder. Execute the following command to start the build:

```
oc start-build test-custom-jboss-amq --from-dir=config --follow
```
In order to target your custom configured AMQ image, you'll need to provide some non-default parameter values to the deployment template to make sure you're targetting your custom image:

```
oc process -f templates/test-amq63-persistent.yaml -p IMAGE_STREAM_NAME=test-custom-jboss-amq IMAGE_STREAM_TAG=latest IMAGE_STREAM_NAMESPACE=<your namespace> | oc apply -f-
```
