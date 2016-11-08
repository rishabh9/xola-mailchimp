#!/usr/bin/env bash
echo "Starting Mailchimp..."
nohup /var/xola/plugins/mailchimp/bin/xola-mailchimp -Dlogger.file=/var/xola/plugins/mailchimp/conf/logback-vagrant.xml -Dconfig.file=/var/xola/plugins/mailchimp/conf/application-vagrant.conf -Dhttp.port=9000 -J-Xms128M -J-Xmx512m -J-server &
