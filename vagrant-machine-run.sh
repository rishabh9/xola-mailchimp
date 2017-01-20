#!/usr/bin/env bash
# Things that you need to run in every vagrant up

# variables
activatorVersion="1.3.10"

echo "=========================================="
echo "Building, deploying and running Mailchimp"
echo "=========================================="
###############################################
# Get activator running
###############################################
sudo service mailchimp stop

sudo rm /var/xola/plugins/mailchimp/RUNNING_PID

cd /vagrant/
/home/vagrant/activator-dist-$activatorVersion/bin/activator clean compile universal:packageZipTarball

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

sudo service mailchimp start
