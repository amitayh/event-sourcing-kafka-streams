#!/usr/bin/env bash

source "$(dirname "$0")/config.sh"

BOOTSTRAP_SERVERS="$(prop 'bootstrap.servers')" \
  java -jar commandhandler/target/scala-2.12/commandhandler.jar
