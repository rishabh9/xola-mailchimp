#!/usr/bin/env bash

# variables - DO NOT CHANGE THESE UNLESS YOU KNOW WHAT YOU'RE DOING!
activatorVersion="1.3.10"
sbtVersion="0.13.11"

# Github credentials - Change these to your credentials
email="rishabh9@gmail.com"
name="Rishabh Joshi"

echo "=========================================="
echo "Provision VM START"
echo "=========================================="

###3### sudo apt-get update

###############################################
# install prerequisits
###############################################
###3### sudo apt-get -y -q upgrade
###3### sudo add-apt-repository ppa:webupd8team/java
###3### sudo apt-get -y -q update
###3### sudo apt-get -y -q install software-properties-common htop
###3### sudo apt-get -y -q install build-essential
###3### sudo apt-get -y -q install tcl8.5

###############################################
# Install Java 8
###############################################
# sudo apt-get install -y openjdk-8-jdk
###3### echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
###3### sudo apt-get -y -q install oracle-java8-installer
###3### sudo update-java-alternatives -s java-8-oracle

###############################################
# In case you need Java 7
###############################################
# sudo apt-get install -y openjdk-7-jdk
# echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
# sudo apt-get -y -q install oracle-java7-installer

###############################################
# Install Git
###############################################
sudo apt-get -y install git
git config --global user.email "$email"
git config --global user.name "$name"

###############################################
# Install imagemagick
###############################################
# sudo apt-get -y install imagemagick

###############################################
# Install Scala
###############################################
# sudo apt-get -y install scala

###############################################
# Install Unzip
###############################################
###3### sudo apt-get -y install unzip

###############################################
# Install NodeJS
###############################################
###3### curl --silent --location https://deb.nodesource.com/setup_4.x | sudo bash -
###3### sudo apt-get -y install nodejs
###3### ln -s /usr/bin/nodejs /user/bin/node
# Add node_modules to environment variables
###3### echo "export NODE_PATH=/usr/local/lib/node_modules" >> ~/.bashrc

###############################################
# Install NPM
###############################################
###3### sudo apt-get -y install npm

###############################################
# Install CoffeeScript
###############################################
# sudo npm install -g coffee-script

###############################################
# Install Bower
###############################################
# sudo npm install -g bower

###############################################
# Install Sass
###############################################
# sudo gem install sass

###############################################
# Install Redis
# More info about it: https://www.digitalocean.com/community/tutorials/how-to-install-and-use-redis
###############################################
# echo "Download Redis..."
# wget http://download.redis.io/releases/redis-stable.tar.gz
# tar xzf redis-stable.tar.gz
# cd redis-stable
# make
# make test
# sudo make install
# cd utils
# sudo ./install_server.sh
# cd /home/vagrant/
# rm redis-stable.tar.gz
# echo "Redis done."

###############################################
# Install PostgreSQL
###############################################
# sudo apt-get -y install postgresql postgresql-contrib postgresql-client-common postgresql-common

###############################################
# Install SBT
###############################################
###3### echo "Download SBT..."
###3### wget http://dl.bintray.com/sbt/debian/sbt-$sbtVersion.deb
###3### sudo dpkg -i sbt-$sbtVersion.deb
###3### sudo apt-get update
###3### sudo apt-get install sbt
###3### rm sbt-$sbtVersion.deb

###3### echo "SBT done."
# Use node as default JavaScript Engine
###3### echo "export SBT_OPTS=\"\$SBT_OPTS -Dsbt.jse.engineType=Node\"" >> ~/.bashrc

###############################################
# Install typesafe activator
###############################################
###3### cd /home/vagrant
###3### echo "Download Typesafe Activator..."
###3### wget http://downloads.typesafe.com/typesafe-activator/$activatorVersion/typesafe-activator-$activatorVersion.zip
###3### unzip -d /home/vagrant typesafe-activator-$activatorVersion.zip
###3### rm typesafe-activator-$activatorVersion.zip
###3### echo "Typesafe Activator done."
# Add activator to environment variables
###3### echo "export PATH=/home/vagrant/activator-dist-$activatorVersion/bin:\$PATH" >> ~/.bashrc

###############################################
# Reset bash
###############################################
###3### source ~/.bashrc

###############################################
# Install MongDB
###############################################
###3### echo "Download MongoDB..."
###3### sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
###3### echo "deb http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.0.list
###3### sudo apt-get update
###3### sudo apt-get -y install mongodb-org
###3### echo "MongoDB done."

###############################################
# Reset bash
###############################################
###3### source ~/.bashrc

###############################################
# Create deployment folders
###############################################
###3### sudo mkdir -p /var/xola/plugins/mailchimp/conf
###3### sudo mkdir -p /var/log/xola

###############################################
# Build & deploy Mailchimp
###############################################
cd /vagrant/
###3### /home/vagrant/activator-dist-$activatorVersion/bin/activator clean compile universal:packageZipTarball
sudo tar -xf /vagrant/target/universal/xola-mailchimp-*.tgz -C /var/xola/plugins/mailchimp --strip-components 1
sudo cp /vagrant/conf/application-vagrant.conf /var/xola/plugins/mailchimp/conf/application-vagrant.conf
sudo cp /vagrant/conf/logback-vagrant.xml /var/xola/plugins/mailchimp/conf/logback-vagrant.xml
export key=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 64 | head -n 1)
sudo sed -i "s/.*play.crypto.*/play.crypto.secret = \"${key}\"/" /var/xola/plugins/mailchimp/conf/application-vagrant.conf

# Just ensuring the start up script is idiot safe
sudo sed -i 's/application.conf/application-vagrant.conf/' /var/xola/plugins/mailchimp/bin/start.sh
sudo sed -i 's/application-test.conf/application-vagrant.conf/' /var/xola/plugins/mailchimp/bin/start.sh
sudo sed -i 's/application-prod.conf/application-vagrant.conf/' /var/xola/plugins/mailchimp/bin/start.sh
sudo sed -i 's/logback.xml/logback-vagrant.xml/' /var/xola/plugins/mailchimp/bin/start.sh
sudo sed -i 's/logback-test.xml/logback-vagrant.xml/' /var/xola/plugins/mailchimp/bin/start.sh
sudo sed -i 's/logback-prod.xml/logback-vagrant.xml/' /var/xola/plugins/mailchimp/bin/start.sh

###############################################
# Configure Mailchimp as a service
###############################################

sudo update-rc.d -f mailchimp remove
sudo cp /vagrant/mailchimp.init /etc/init.d/mailchimp
sudo chmod a+x /etc/init.d/mailchimp
sudo update-rc.d mailchimp defaults

sudo service mailchimp restart

###############################################
# Configure Hostname ans Hosts file
###############################################

###3### sudo hostname mailchimp
###3### sudo sh -c "echo 'mailchimp' > /etc/hostname"

###3### sudo sh -c "echo '' >> /etc/hosts"
###3### sudo sh -c "echo '10.10.10.10    xola.local xola.dev' >> /etc/hosts"
###3### sudo sh -c "echo '10.10.10.12    legolas.local legolas.dev' >> /etc/hosts"
###3### sudo sh -c "echo '' >> /etc/hosts"

###############################################
# Show installation summary
###############################################
echo "=========================================="
echo "Provision VM summary"
echo "=========================================="
echo "Dependencies installed:"
echo " "
echo "jdk version:"
javac -version
echo " "
echo "NodeJS version:"
node -v
echo " "
echo "NPM version"
npm -v
echo " "
# echo "CoffeeScript version:"
# coffee -v
# echo " "
# echo "Bower version:"
# bower -v
# echo " "
# echo "Sass version:"
# sass -v
# echo " "
# echo "Redis version"
# redis-server -v
# echo " "
# echo "PostgreSQL version"
# psql --version
# echo " "
echo "mongoDB version"
mongod --version
echo " "
echo "=========================================="
echo "Provision VM finished"
echo "=========================================="
