#!/usr/bin/env bash

mvn clean install && docker build -t expressotoday/esearch-rss . && docker push expressotoday/esearch-rss