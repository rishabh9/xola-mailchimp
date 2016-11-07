#!/usr/bin/env bash
echo "Stopping Mailchimp..."
kill `cat /var/xola/mailchimp/RUNNING_PID`
