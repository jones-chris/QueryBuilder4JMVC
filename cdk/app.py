#!/usr/bin/env python3
import sys

from aws_cdk import core

from cdk.cdk_stack import Qb4jStack


app = core.App(outdir='cdk.out')

env = app.node.try_get_context('env')
print(env)

Qb4jStack(app, "QB4J", env=env)

app.synth()
