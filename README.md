Description:
============

This project implements a secure workload runner on a remote machine. 
The commands can be specified in a config/workload.json file and tag it with stages. While this framework is extensible and configurable to run any commands, the current implementation does this for spinning up docker containers and monitoring their statuses and capturing logs.

Config Stages:
================
SpinUp: Can bring up arbitary number of docker containers. <br />
Stats:  Collects Host and container stats. CPU, Mem, I/O. <br />
Logs:   Captures logs from all the running containers. <br />
