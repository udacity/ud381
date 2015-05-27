#!/usr/bin/env bash

 # - Install JDK 8 since default is 6 
 # - sudo apt-get install software-properties-common python-software-properties
 # - sudo add-apt-repository ppa:webupd8team/java
 # - sudo apt-get --yes update
 # - sudo apt-get  --yes --force-yes install oracle-java8-installer
 # - sudo apt-get  --yes --force-yes install oracle-java8-set-default
 # - sudo apt-get --yes update




sudo apt-get install software-properties-common python-software-properties
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get --yes update
sudo apt-get  --yes --force-yes install oracle-java8-installer
sudo apt-get  --yes --force-yes install oracle-java8-set-default
sudo apt-get --yes upgrade


echo "Done "