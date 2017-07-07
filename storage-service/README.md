# incubator-storage-service

## Writing files

```
curl --data-binary @"filename" -H "Authorization: Bearer <access_token>" 'https://testing.datascience.ch:9000/write/folder%2Ffilename'
```

## Reading files

```
curl -H "Authorization: Bearer <access_token>" 'https://testing.datascience.ch:9000/read/folder%2Ffilename'
```

## Running locally:

The swift backend is currently hardcoded to the SWITCH one. The keycloak backend is also hardcoded to the one deployed in the testing environment. (They should be both accessible from the internet however). This will be changed soon.

```
sbt docker:publishLocal

docker run -d -t -i -e SWIFT_PASSWORD='<the_swift_password>' -e PLAY_APPLICATION_SECRET='<some_random_string>' -p 9000:9000 storage-service:1.0-SNAPSHOT
```
