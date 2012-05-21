#!/bin/sh

export REDISTOGO_URL='redis://abc:test123@127.0.0.1:6379/'
lein run
