#!/usr/bin/python2.7
import xmlrpclib
import sys

s = xmlrpclib.ServerProxy('http://localhost:6800/rpc')
gid = s.aria2.addUri([sys.argv[1]], dict(dir=sys.argv[2], out=sys.argv[3]))

print 'Success#%s' % gid
