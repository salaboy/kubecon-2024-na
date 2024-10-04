#!/bin/bash

curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"id":"asd-123","device":"device1","payload":{"content" : "abc"}}' \
  http://localhost:8080/device/events