#!/usr/bin/env bash

# This script is for deploying within the vagrant environment only.
# DO NOT USE IT ON PRODUCTION OR ANY OTHER PLACE OUTSIDE VAGRANT.

echo "Deploying Mailchimp..."
activator clean compile universal:packageZipTarball && tar -xf mailchimp-*.tgz -C /var/xola/plugins/mailchimp --strip-components 1
