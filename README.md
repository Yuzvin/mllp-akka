# akka-mllp

##Prerequisites
Before starting application server launch `postresql` service and create `mllp` database:
```
$ psql postgres -U postgres
postgres=# CREATE DATABASE mllp;
postgres=# GRANT ALL PRIVILEGES ON DATABASE mllp TO postgres;
```
##How to run
There is built-in `sbt` launcher, added to project files. You may also need to change rights for launcher:
```
$ chmod u+x ./sbt
$ chmod u+x ./sbt-dist/bin/sbt
```
After starting application server there is starting message exchanging process:
`MllpSender` start sending messages to `MllpReceiver` via MLLP protocol.
Go to root directory of project and then:
* For starting application server:
`./sbt "run"`
* For running tests:
`./sbt "test"`
* While application is running, you can also send your MLLP-formatted message in next template:
`{MESSAGE_TEXT}|{SYSTEM_NAME}`
Message should be wrapped into MLLP-specified block bytes.
##API description
Date pattern: `dd.MM.yyyy HH:mm:ss`
`Message` data model example:
```json
{
"content": "Message 1",
"creationDate": "09.05.2019 01:01:01",
"id": 1,
"systemName": "Laboratory"
}
```
`MessageDistribution` data model example:
 
```json
{
  "startDate": "01.01.2019 00:00:00",
  "endDate": "01.01.2020 00:00:00",
  "messages": [{
    "content": "Message 1",
    "creationDate": "09.05.2019 01:01:01",
    "id": 1,
    "systemName": "Laboratory"
  }]
}
```
HTTP-API:
* Response status codes:
```
200 - Success
204 - Success, but content not found
400 - Bad request
500 - Internal server error
```
* Get list of N latest messages by specified date:
```
GET /api/v1/messages
Required query params: "amount", "date"
Return: array of "Message"
```
* Get message by id:
```
GET /api/v1/messages/:id
Return: "Message" or empty response
```
* Get distribution of messages by years by system:
```
GET /api/v1/messages/system/:systemName
Required query param: "distributedBy" with value "year"
Return: array of "MessageDistribution"
```
* Get distribution of messages by week by system for last four weeks for specified date:
```
GET /api/v1/messages/system/:systemName
Required query params: "date", "distributedBy" with value "week"
Return: array of MessageDistribution
```