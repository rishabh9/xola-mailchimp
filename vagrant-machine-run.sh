#!/usr/bin/env bash
# Things that you need to run in every vagrant up

# variables
activatorVersion="1.3.10"

echo "=========================================="
echo "Set tasks to run"
echo "=========================================="
###############################################
# Get activator running
###############################################
sudo service mailchimp stop
cd /vagrant/
/home/vagrant/activator-dist-$activatorVersion/bin/activator clean compile universal:packageZipTarball
sudo tar -xf /vagrant/target/universal/xola-mailchimp-*.tgz -C /var/xola/plugins/mailchimp --strip-components 1
sudo service mailchimp start
