#!/bin/bash

oc apply -f KafkaOperator.yaml
export INSTALLPLAN=`oc get installplan -n openshift-operators | grep amqstreams | grep install | awk '{print $1;}'`
oc patch installplan $INSTALLPLAN -n openshift-operators --type merge --patch '{"spec":{"approved":true}}'
while [[ `oc get pods -n openshift-operators | grep amq-streams | grep -c Running` == 0 ]]
 do
  	echo "Waiting for streams operator"
  	sleep 5;
 done
oc apply -f Kafka.yaml
while [[ `oc get Kafka | grep registry-cluster | awk '{print $4;}'` != "True" ]] && [[ "$#" -ne 5 ]]
 do
 	echo "Waiting for kafka cluster to reach ready"
 	sleep 5;
 done
 oc apply -f RegistryTopic.yaml
 while [[ `oc get KafkaTopic | grep kafkasql-journal | awk '{print $5;}'` != "True" ]] 
 do
 	echo "Waiting for registry topic to reach ready"
 	sleep 5;
 done
 oc apply -f RegistryUser.yaml
 while [[ `oc get KafkaUser | grep registry-user | awk '{print $5;}'` != "True" ]] 
 do
 	echo "Waiting for registry user to reach ready"
 	sleep 5;
 done
 oc apply -f RegistryOperator.yaml
 export INSTALLPLAN=`oc get installplan -n openshift-operators | grep service-registry-operator | grep install | awk '{print $1;}'`
 oc patch installplan $INSTALLPLAN -n openshift-operators --type merge --patch '{"spec":{"approved":true}}'
 
 while [[ `oc get pods -n openshift-operators | grep apicurio-registry | grep -c Running` == 0 ]]
 do
  	echo "Waiting for registry operator"
  	sleep 5;
 done
  oc apply -f Registry.yaml
