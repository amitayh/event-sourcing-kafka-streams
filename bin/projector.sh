#!/usr/bin/env bash

source "$(dirname "$0")/config.sh"

BOOTSTRAP_SERVERS="$(prop 'bootstrap.servers')" \
  DB_DRIVER="$(prop 'db.driver')" \
  DB_URL="$(prop 'db.url')" \
  DB_USER="$(prop 'db.user')" \
  DB_PASS="$(prop 'db.pass')" \
  java -jar listprojector/target/scala-2.12/listprojector.jar
