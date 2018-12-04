#!/usr/bin/env bash

config_file=${1}

function prop {
    grep "${1}" ${config_file} | cut -d '=' -f2
}
