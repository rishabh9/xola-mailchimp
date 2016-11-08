#!/usr/bin/env bash
echo "Stopping Mailchimp..."
kill `cat /var/xola/plugins/mailchimp/RUNNING_PID`
