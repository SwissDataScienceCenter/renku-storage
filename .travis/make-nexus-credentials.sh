#!/bin/bash

CREDENTIALS_DIR="$HOME/.ivy2"
mkdir -p "$CREDENTIALS_DIR"

CREDENTIALS="$CREDENTIALS_DIR/.credentials"
echo "realm=Sonatype Nexus Repository Manager" > $CREDENTIALS
echo "host=$NEXUS_HOST" >> $CREDENTIALS
echo "user=$NEXUS_USER" >> $CREDENTIALS
echo "password=$NEXUS_PASSWORD" >> $CREDENTIALS

