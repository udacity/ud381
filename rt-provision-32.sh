#!/bin/bash -i

set -euo pipefail

sudo apt-get update -y

sudo apt-get -y install default-jdk maven vim zookeeper zookeeperd redis-server \
    python-software-properties python-pip python tree

sudo pip install flask redis

echo "Storm..."
# TODO maybe make this use the best mirror always?
sudo mkdir /opt/storm
cd /opt/storm
sudo wget http://mirror.cogentco.com/pub/apache/incubator/storm/apache-storm-0.9.2-incubating/apache-storm-0.9.2-incubating.tar.gz
sudo tar xvzf apache-storm-0.9.2-incubating.tar.gz
sudo rm apache-storm-0.9.2-incubating.tar.gz
sudo chmod +x /opt/storm/apache-storm-0.9.2-incubating/bin/storm
sudo ln -s /opt/storm/apache-storm-0.9.2-incubating/bin/storm /usr/bin/storm
