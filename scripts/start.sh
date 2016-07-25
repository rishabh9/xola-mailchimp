#!/usr/bin/env bash
echo "Starting Legolas..."
nohup ./xola-mailchimp -Dlogger.file=/var/xola/plugins/mailchimp/conf/logback-prod.xml -Dconfig.file=/var/xola/plugins/mailchimp/conf/application-prod.conf -Dhttp.port=9099 -J-Xms128M -J-Xmx512m -J-server &
