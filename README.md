finmgr
===

![Travis CI](https://travis-ci.org/ksean/finmgr.svg?branch=master)

A financial transaction framework

![Project Overview](finmgr.png "finmgr project overview")


## Requirements

### finmgr-core, finmgr-lib

Java >= 11

Maven >= 3.6.3

### finmgr-web

Yarn >= 1.22.5

Node >= 14.16.1

## Installation

`mvn install`

## Running 

Project lifecycle is managed through Maven. `finmgr-web` (frontend) and `finmgr-core` (backend) can be launched independently.

#### finmgr-web

`yarn`

`yarn run start`

#### finmgr-core

`mvn package`

Use packaged jar file to deploy server API

## Testing

`mvn test`

## License

[GNU GPLv3](https://www.gnu.org/licenses/)
