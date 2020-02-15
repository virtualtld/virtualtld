#!/usr/bin/env bash

COMMAND="$1"

if [ -z "$COMMAND" ]
then
    echo "missing command:"
    echo "virtualtld download <url> [out]"
    echo "virtualtld serve <webroot>"
    exit 0
fi

shift
exec ./gradlew $COMMAND --args="$1 $2"
