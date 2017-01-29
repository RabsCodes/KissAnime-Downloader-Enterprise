#!/bin/bash

aria2c -x 10 -i $1 2>&1 | tee -a $1.log
