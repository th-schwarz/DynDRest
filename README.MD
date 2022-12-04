# DynDRest

Dynamic DNS Rest service

![Build Status](https://github.com/th-schwarz/DynDRest/actions/workflows/maven.yml/badge.svg)
[![GitHub license](https://img.shields.io/github/license/th-schwarz/DynDRest)](https://github.com/th-schwarz/DynDRest/blob/develop/LICENSE)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=th-schwarz_DynDRest&metric=alert_status)](https://sonarcloud.io/dashboard?id=th-schwarz_DynDRest)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=th-schwarz_DynDRest&metric=security_rating)](https://sonarcloud.io/dashboard?id=th-schwarz_DynDRest)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=th-schwarz_DynDRest&metric=coverage)](https://sonarcloud.io/summary/new_code?id=th-schwarz_DynDRest)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=th-schwarz_DynDRest&metric=ncloc)](https://sonarcloud.io/dashboard?id=th-schwarz_DynDRest)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=th-schwarz_DynDRest&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=th-schwarz_DynDRest)

Give it a try!   [![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/th-schwarz/DynDRest?include_prereleases)](https://github.com/th-schwarz/DynDRest/releases)

DynDRest is a simple REST-service for dynamic DNS and is a more general variant of [DDAuto](https://github.com/th-schwarz/DDAuto). The basic idea is to have multiple implementations for different dns providers. [AutoDNS](https://www.internetx.com/en/domains/autodns) will be one of them.

The restful-api can be used with many routers, for example the AVM Fritz!Box.
DynDRest can be executed by `commandline`, `init.d` or `systemd`.

If you find a bug or certain features are missing, don't hesitate to file an issue on [Github](https://github.com/th-schwarz/DynDRest/issues).

![Project Status](https://img.shields.io/badge/project%20status-BETA-orange)

DynDRest is kindly supported by <br> <a href="https://jb.gg/OpenSourceSupport"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA.png" width="250px" alt="IntelliJ IDEA logo."></a>

## Disclaimer

I'm not responsible for any data loss, hardware damage or broken keyboards. This guide comes without any warranty!

## Big picture

DynDRest is running as a service. A client can access the service via url and basic-auth. Let's assume DynDRest is running on `localhost`, then we can update the IPv4 and IPv6 address of the host _mydyndns.domain.com_ by calling the following curl command:

```bash
curl -u dyndns:test123 -i \ 
   "http://localhost:8081/update/mydyndns.domain.com/?apitoken=1234567890abcdf&ipv4=127.1.2.4&ipv6=2a03:4000:41:32::2"
```

For each host an api-token must be defined. If the api-token doesn't match the host, the update will be failed. Due to this security feature, DynDRest can be used by different people. They can't update the IPs each other.

## Build a docker image

See [Dockerfile](Dockerfile) for multistage build. You can execute the build by running:

```bash
docker build -t dyndrest:latest -f Dockerfile .
```

## Configuration and start

DynDRest is a spring boot application, that's why the application is extremely customizable just by configuration! Details of the configuration can be found here: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#application-properties and https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#actuator.monitoring.customizing-management-server-port

Side note: To avoid the use of the java keystore tool, DynDRest could be run behind a proxy. Corresponding headers are set by default.

Here is the suggested directory/file structure:

```bash
├── /opt/dyndrest
│   ├── dyndrest.yml
│   ├── dyndrest-0.1.0.jar
│   ├── dyndrest.jar -> dyndrest-0.1.0.jar
│   ├── dyndrest-update.log   (log of the updates)
│   ├── logback.xml         (logback configuration for application and update logoging, optional)
│   ├── logback-update.xml  (extra logback configuration for update logging, optional include) 
```

Side note: If the names of the configuration files are kept as above, they will be found and loaded automatically.

**dyndrest.yml** defines the individual properties and will be merged with the default properties in the classpath, therefore the file can be kept as small as possible. A minimal configuration example can be found further below. If the file is inside the working directory, it is loaded automatically. The complete configuration setings can be found [here](https://github.com/th-schwarz/DynDRest/wiki/DynDRest-Configuration). <br>
Important: The basic-auth, the api-tokens and the credentials for AutoDNS should be defined in this file!<br>
The configuration of Logback can be split into two files, one for the application logging and one for a the well-formatted zone update log. The second one is the one on which the log view is based. <br>
For further information have look at [Logback Configuration](https://github.com/th-schwarz/DynDRest/wiki/Logback-Configuration) wiki article.

### cmd line

The start by command line looks like:

```bash
java -jar /opt/dyndrest/dyndrest.jar
```

BTW: This project uses _spring-doc_ to document the routes. The endpoints for this and the _swagger-ui_ are disabled by default.

### systemd

DynDRest can also be started easily as a service. For further information, see [https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#deployment.installing.nix-services](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#deployment.installing.nix-services)

### docker

To run the docker image with your configuration file.

```bash
docker run --rm \
  --name dyndrest \
  --volume ${PWD}/src/test/resources/example.yml:/opt/dyndrest/dyndrest.yml \
  --volume ${PWD}/src/test/resources/logback.xml:/opt/dyndrest/logback.xml \
  -it dyndrest:latest
```

> Note: container exists with error because of example data in `dyndrest.yml`

## Configuration

This is a minimal configuration example for your individual properties file _dyndrest.yml_ using the provider _domainrobot_:

```yaml
spring:
  security:
    user:
      name: dyndns
      password: test123
      
dyndrest:
  provider: domainrobot

domainrobot:
  autodns:
    password: pwd_t
    user: user_t
  zones:
    - name: dynhost.info
      ns: ns.domain.info
      hosts:
        - myhost:1234567890abcdef
```

The 'zone' section defines a host ```myhost.dynhost.info``` with the api-token `1234567890abcdef`.

## Routes

All routes are secured by basic-auth. A detailed description of the routes can be found at the [OAS3 documentation](https://htmlpreview.github.io/?https://github.com/th-schwarz/DynDRest/blob/develop/docs/index.html).

There are two additional routes which deliver web pages:

* [/]: A simple greeting page which is enabled by default. It can be disabled by setting the property ```dyndrest.greeting-enabled=false```.
* [/log]: Delivers a simple web page to view the zone update logs and is secured by basic-auth and can be configured by setting the two properties ```dyndrest.update-log-user-name``` and ```dyndrest.update-log-user-password```. It is enabled by default but it can be disabled by setting the property ```dyndrest.update-log-page-enabled=false```.

## Suggested AutoDNS setup

For security reasons, it makes sense to create a separate owner for the zone updated by DynDRest. This owner just needs the permission for zone-info and zone-updates!

## Setup a router for dynamic DNS

As an example, let's have a look at the setup of dynamic DNS in the [Fritz!Box 7590](https://service.avm.de/help/en/FRITZ-Box-7530/019p2/hilfe_dyndns).
The following settngs are required:

* **DynDNS Provider**: User-defined
* **Domain name**: The hostname for which the IPs should be updated.
* **Username / Password**: The credentials for basic-auth.
* **Update-URL**: {baseUrl}/update/\<domain\>/?apitoken=[yourApitoken]&ipv4=\<ipaddr\>&ipv6=\<ip6addr\>
  </br> If both IP parameters are omitted, an attempt is made to fetch the remote IP.
