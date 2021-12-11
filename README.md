Udacity and Twitter bring you Real-Time Analytics with Apache Storm
=====

Join the course for free:
www.udacity.com/course/ud381


### Serge's changes

 - updated to point to fixed apache storm repo
 - added provision task for vagrant 
 - run or re-run vagrant provision --provision-with bootstrap - will remove old, reinstall storm
 - run or re-run vagrant provision --provision-with jdk8  - this will upgrade to jdk8 and set it to default


 - Install JDK 8 since default is 6 
 - sudo apt-get install software-properties-common python-software-properties
 - sudo add-apt-repository ppa:webupd8team/java
 - sudo apt-get update
 - sudo apt-get install oracle-java8-installer




### Environmental variables need to be set:

=====

export TWITTER_CKEY=...
export TWITTER_SKEY=...
export TWITTER_TOKEN=..
export TWITTER_SECRET=...
