#!/bin/bash

if [ -f "logfile.txt" ]; then
    rm -rf logfile.txt
fi

while true; do
    echo "Hello $$" >> logfile.txt
    sleep 2
done