#!/bin/bash

find ./cdk/cdk.out -name "*.template.json" > ./cf_stacks.txt

while read line; do
  if [ $line != "./cdk/cdk.out/CodeBuildStack.template.json" ]; then
    aws cloudformation deploy --template-file $line --stack-name "test" --capabilities CAPABILITY_NAMED_IAM
  fi;
done < ./cf_stacks.txt