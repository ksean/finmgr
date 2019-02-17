finmgr
===

![Travis CI](https://travis-ci.org/ksean/finmgr.svg?branch=master)

A financial transaction framework

![Project Overview](finmgr.png "finmgr project overview")


## Requirements

Java >= 8

## Installation

`mvn install`

## Running 

Project lifecycle is managed through Maven. `finmgr-web` (frontend) and `finmgr-core` (backend) can be launched independently.

#### finmgr-web

`npm install`

`npm start`

#### finmgr-core

`mvn package`

Use packaged jar file to deploy server API

## Testing

`mvn test`

## License

[GNU GPLv3](https://www.gnu.org/licenses/)
