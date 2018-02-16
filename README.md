# renga-storage
Renga Storage Service

**Renga is currently undergoing a major restructuring effort. For a preview, you can look
at the development branch, but keep in mind it is highly volatile.**

Documentation: https://renga.readthedocs.io/en/latest/developer/services/storage_service.html

Support for git backends has been added. LFS is managed by the service and delegated to the given object store backend.

## Development
Building is done using [sbt](http://www.scala-sbt.org/).

To create a docker image:
```bash
sbt docker:publishLocal
[...]
[info] Successfully tagged renga-storage:<version>
[info] Built image renga-storage:<version>
```

Image name and tag can be manipulated with sbt settings, see
[sbt-native-packager](https://sbt-native-packager.readthedocs.io/en/v1.2.2/formats/docker.html).

For local development, it is recommended to use the local storage backend
by setting the environment variable `STORAGE_BACKEND_LOCAL_ENABLED` to `true`.

| Variable name  |  default  |  description  |
|---|---|---|
| STORAGE_BACKEND_DB_PASSWORD  |  storage  |  the password for the user `storage` for the postgres database  |
| STORAGE_BACKEND_LOCAL_ENABLED  |  false  |  set to `true` to enable the local storage backend |
| LOCAL_STORAGE_ROOT | /data  | the root folder in which buckets will be created as subfolders |
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
| LOCAL_STORAGE_GIT_ROOT | /data | the root folder in which repositories will be created as subfolders |
| STORAGE_BACKEND_GITLAB_ENABLED | false | set to `true` to enable the gitlab storage backend |
| GITLAB_URL |  | the root url of gitlab |
| GITLAB_USER |  | the gitlab user |
| GITLAB_PASS |  | the gitlab user's private token |
| RENGA_ENDPOINT |  | the url to reach renga from the users perspective |
