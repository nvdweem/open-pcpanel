# Read Me First

This project was started to reverse engineer the pcpanel software and to see if I could make some improvements (like plugin support). I got bored building it so it's not very functional.

The c++ part mostly seems to do what it should, control volume using jna calls from Java. The interface with the PCPanel Pro works mostly (setting lights, getting events) but the interface isn't great and doesn't work very nicely.

The project could be used by someone else to build a fully functioning alternative.

The instructions for the PCPanel Pro and Mini are different so to make this compatible with the Mini it would require some more reverse engineering those commands.


# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.0/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.0/gradle-plugin/reference/html/#build-image)
* [Spring Native Reference Guide](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [Configure the Spring AOT Plugin](https://docs.spring.io/spring-native/docs/0.10.0-SNAPSHOT/reference/htmlsingle/#spring-aot-gradle)

## Spring Native

This project has been configured to let you generate a lightweight container running a native executable.
Docker should be installed and configured on your machine prior to creating the image, see [the Getting Started section of the reference guide](https://docs.spring.io/spring-native/docs/0.10.0-SNAPSHOT/reference/htmlsingle/#getting-started-buildpacks).

To create the image, run the following goal:

```
$ ./gradlew bootBuildImage
```

Then, you can run the app like any other container:

```
$ docker run --rm pcpanel:0.0.1-SNAPSHOT
```
