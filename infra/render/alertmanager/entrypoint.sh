#!/bin/sh
set -eu

: "${ALERT_WEBHOOK_HOSTPORT:?ALERT_WEBHOOK_HOSTPORT is required}"
sed "s|__ALERT_WEBHOOK_HOSTPORT__|${ALERT_WEBHOOK_HOSTPORT}|g" \
    /etc/alertmanager/alertmanager.yml.template > /tmp/alertmanager.yml

exec /bin/alertmanager --config.file=/tmp/alertmanager.yml "$@"