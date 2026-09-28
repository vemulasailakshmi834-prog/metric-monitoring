#!/bin/sh
set -eu

export GF_SERVER_HTTP_ADDR=0.0.0.0
export GF_SERVER_HTTP_PORT="${PORT:-10000}"
exec /run.sh