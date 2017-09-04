#!/bin/bash

user=daemon

if [[ "${STORAGE_BACKEND_LOCAL_ENABLED,,}" = "true" ]]; then
    echo "local storage enabled..."
    file=$(ls -A /data | head -n1)
    if [[ -z $file ]]; then
        echo "cannot determine user, running as $user"
    else
        file="/data/$file"
        uid=$(stat -c "%u" $file)
        gid=$(stat -c "%g" $file)
        if [[ $uid -ne 0 ]]; then # .gitkeep is 0:0 os osxfs mounts, do nothing
            addgroup -g $gid daemon2
            adduser -h /sbin -g daemon2 -s /sbin/nologin -G daemon2 -D -u $uid daemon2
            addgroup daemon2 daemon
            addgroup daemon2 bin
            addgroup daemon2 adm
            user=daemon2
            chown -R "$uid:$gid" .
            echo "running as $user, uid=$uid, gid=$gid"
        fi
    fi
fi

su -s /bin/sh -c '"$@"' "$user" -- _ "$@"
