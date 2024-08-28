finmgr
===

[![CircleCI](https://circleci.com/gh/ksean/finmgr.svg?style=svg)](https://circleci.com/gh/ksean/finmgr)

A financial transaction framework

![Project Overview](finmgr.png "finmgr project overview")


## Requirements

### finmgr-core, finmgr-lib

Java >= 21

### finmgr-web

Yarn >= 4.4.1

Node >= 20.17.0

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

[GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
