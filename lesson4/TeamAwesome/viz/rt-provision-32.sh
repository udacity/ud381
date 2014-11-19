#!/bin/bash

echo "Real-Time Provisioning...."

echo "Java JDK..."
sudo apt-get install default-jdk -y

echo "Storm..."
#sudo wget http://apache.spinellicreations.com/incubator/storm/apache-storm-0.9.1-incubating/apache-storm-0.9.1-incubating.zip
#sudo unzip -o /media/sf_VirtualBoxUbuntuShared/apache-storm-0.9.1-incubating.zip
sudo wget http://www.trieuvan.com/apache/incubator/storm/apache-storm-0.9.2-incubating/apache-storm-0.9.2-incubating.zip
sudo unzip -o $(pwd)/apache-storm-0.9.2-incubating.zip
# use storm.0.9.2 for now...confirming with Twitter
sudo ln -s $(pwd)/apache-storm-0.9.2-incubating/ /usr/share/storm
sudo ln -s /usr/share/storm/bin/storm /usr/bin/storm

echo "Lein..."
sudo wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
sudo mv lein /usr/bin
sudo chmod 755 /usr/bin/lein
lein

echo "Kafka..."
#sudo wget http://www.motorlogy.com/apache/kafka/0.8.1.1/kafka_2.9.2-0.8.1.1.tgz
#sudo tar -xvzf kafka_2.9.2-0.8.1.1.tgz

echo "Sublime..."
#sudo wget http://c758482.r82.cf2.rackcdn.com/sublime_text_3_build_3047_x64.tar.bz2
#sudo tar vxjf sublime_text_3_build_3047_x64.tar.bz2
#sudo mv sublime_text_3 /opt/
#sudo ln -s /opt/sublime_text_3/sublime_text /usr/bin/sublime

sudo wget http://c758482.r82.cf2.rackcdn.com/sublime_text_3_build_3047.tar.bz2
sudo tar vxjf Sublime\ Text\ 2.tar.bz2
sudo mv Sublime\ Text\ 2 /opt/
sudo ln -s /opt/Sublime\ Text\ 2/sublime_text /usr/bin/sublime

echo "Maven run..."
#cd /vagrant/storm-hack
#mvn -f m4-pom.xml clean
#mvn -f m4-pom.xml compile
#mvn -f m4-pom.xml package
#mvn -f m4-pom.xml clean
#cd /vagrant

echo "IntelliJ..."
sudo wget http://download-cf.jetbrains.com/idea/ideaIC-13.1.3.tar.gz
sudo tar -xvzf ideaIC-13.1.3.tar.gz
#sudo ln -s idea-IC-135.909/bin/idea.sh /usr/bin/idea

echo "Git..."
sudo apt-get install git-core -y

echo "Redis (Python)..."
sudo pip install redis

echo "MongoDB...removed"
#sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
#sudo echo "deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen" | sudo tee -a /etc/apt/sources.list.d/10gen.list
#sudo apt-get -y update
#sudo apt-get -y install mongodb-10gen

echo "Nodejs...(puppet attempt failed uy_nodejs-32...removed"
#sudo wget http://nodejs.org/dist/v0.10.29/node-v0.10.29-linux-x86.tar.gz
#sudo tar -xvzf node-v0.10.29-linux-x86
#sudo /vagrant/node-v0.10.29-linux-x86
#linking doesn't work....
#sudo apt-add-repository ppa:chris-lea/node.js -y
#sudo apt-get update -y
#sudo apt-get install nodejs -y

echo "Adding from VagrantFile...."
sudo ufw disable

sudo apt-get update -y

sudo apt-get install maven -y

sudo apt-get install vim -y

sudo apt-get --yes install zookeeper zookeeperd -y

sudo apt-get install redis-server -y

sudo apt-get install python-software-properties -y

sudo apt-get install python-pip -y

sudo pip install flask

sudo pip install redis

