# renku-storage
Renku Storage Service

Documentation: https://renku.readthedocs.io/en/latest/developer/services/storage_service.html

Support for git backends has been added. LFS is managed by the service and delegated to the given object store backend.

## Building the Docker image
The Dockerfile is self sufficient to build the Docker image. You can simply run:
```bash
docker build -t <tag> .
```
The first build can take more than 5 minutes as maven artefacts are downloaded but subsequent builds should use cached layers.

Alternatively, you can use the Makefile to get git branch and commit based tags:
```bash
make
> Successfully built cb53b5cd36eb
> Successfully tagged renku/renku-storage:08a076cf5c49
> docker tag renku/renku-storage:08a076cf5c49 renku/renku-storage:development
```
Two tags were defined: `renku/renku-storage:08a076cf5c49` and `renku/renku-storage:development` where `08a076cf5c49` is the commit sha1 (truncated) and `development` is the git branch name.

## Development
Building is done using [sbt](http://www.scala-sbt.org/).

To run tests:
```bash
sbt test
```

For local development, it is recommended to use the local storage backend
by setting the environment variable `STORAGE_BACKEND_LOCAL_ENABLED` to `true`.

| Variable name  |  default  |  description  |
|---|---|---|
| STORAGE_BACKEND_DB_URL  |  jdbc:postgresql://db:5432/storage  |  the url for the postgres database  |
| STORAGE_BACKEND_DB_USER  |  storage  |  the user for the postgres database  |
| STORAGE_BACKEND_DB_PASSWORD  |  storage  |  the password for the postgres database  |
| STORAGE_BACKEND_LOCAL_ENABLED  |  false  |  set to `true` to enable the local storage backend |
| LOCAL_STORAGE_ROOT | /data/obj  | the root folder in which buckets will be created as subfolders |
| STORAGE_BACKEND_SWIFT_ENABLED | false | set to `true` to enable the swift storage backend |
| SWIFT_USERNAME |  | the swift username |
| SWIFT_PASSWORD |  | the swift password |
| SWIFT_AUTH_URL |  | the keystone url, e.g. https://keystone.example.com:5000/v2.0/tokens |
| SWIFT_PROJECT |  | the id of the project to use |
| STORAGE_BACKEND_S3_ENABLED | false | set to `true` to enable the s3 storage backend |
| S3_URL |  | the S3 endpoint url |
| S3_ACCESS_KEY |  | the S3 access key  |
| S3_SECRET_KEY |  | the S3 secret key |
| STORAGE_BACKEND_AZURE_ENABLED | false | set to `true` to enable the azure storage backend |
| AZURE_CONNECTION_STRING |  | the complete connection string for the Azure Blob Storage |
| STORAGE_BACKEND_LOCALGIT_ENABLED | false | set to `true` to enable the local git storage backend |
| LOCAL_STORAGE_GIT_ROOT | /data/repo | the root folder in which repositories will be created as subfolders |
| STORAGE_BACKEND_GITLAB_ENABLED | false | set to `true` to enable the gitlab storage backend |
| GITLAB_URL |  | the root url of gitlab |
| GITLAB_USER |  | the gitlab user |
| GITLAB_PASS |  | the gitlab user's private token |
| RENKU_ENDPOINT | http://localhost | the url to reach renku from the users perspective |
| RENKU_STORAGE_DEFAULT_LFS_BACKEND | local | the default backend to create buckets if none is specified for a git repository |
