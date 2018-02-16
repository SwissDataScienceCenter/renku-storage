# renga-storage
Renga Storage Service

**Renga is currently undergoing a major restructuring effort. For a preview, you can look
at the development branch, but keep in mind it is highly volatile.**

Documentation: https://renga.readthedocs.io/en/latest/developer/storage_service.html

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
