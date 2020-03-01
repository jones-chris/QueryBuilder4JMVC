#!/usr/bin/env python3
import sys

from aws_cdk import core

from cdk.cdk_stack import Qb4jStack


first_arg = sys.argv[1].split('=')
if len(first_arg) == 2:
    first_arg_name = first_arg[0]
    first_arg_value = first_arg[1]
    if first_arg_name == 'env':
        env = first_arg_value
    else:
        raise ValueError('env is the expected first argument, like env=dev')
else:
    raise ValueError('env is the expected first argument, like env=dev')

print(sys.argv)
print(env)

app = core.App(outdir='cdk.out')

# env = app.node.try_get_context('env')
# print(env)

Qb4jStack(app, "QB4J", env=env)

app.synth()
