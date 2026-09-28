#!/bin/sh
set -eu

: "${SAMPLE_APP_1_HOSTPORT:?SAMPLE_APP_1_HOSTPORT is required}"
: "${SAMPLE_APP_2_HOSTPORT:?SAMPLE_APP_2_HOSTPORT is required}"
: "${SAMPLE_APP_3_HOSTPORT:?SAMPLE_APP_3_HOSTPORT is required}"
: "${ALERTMANAGER_HOSTPORT:?ALERTMANAGER_HOSTPORT is required}"

sed \
    -e "s|__SAMPLE_APP_1_HOSTPORT__|${SAMPLE_APP_1_HOSTPORT}|g" \
    -e "s|__SAMPLE_APP_2_HOSTPORT__|${SAMPLE_APP_2_HOSTPORT}|g" \
    -e "s|__SAMPLE_APP_3_HOSTPORT__|${SAMPLE_APP_3_HOSTPORT}|g" \
    -e "s|__ALERTMANAGER_HOSTPORT__|${ALERTMANAGER_HOSTPORT}|g" \
    /etc/prometheus/prometheus.yml.template > /tmp/prometheus.yml

exec /bin/prometheus --config.file=/tmp/prometheus.yml "$@"