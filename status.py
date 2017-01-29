#!/usr/bin/python2.7
import xmlrpclib
import sys

s = xmlrpclib.ServerProxy('http://localhost:6800/rpc')
stats = s.aria2.getGlobalStat()

print 'Success#%s#%s' % (stats['numActive'], stats['numWaiting'])
