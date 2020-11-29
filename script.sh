# 1. Create IAM Role
aws iam create-role --role-name state-machine-role --assume-role-policy-document file://cloudwatch-event-role-trust-policy.json

# 2. Attach Policy to role
#Don't forgot to update State machine ARN in state-machine-execution-policy.json file. 
aws iam put-role-policy --role-name state-machine-role --policy-name state-machine-execution-policy --policy-document file://state-machine-execution-policy.json

# 3. Create CloudWatch Event
aws events put-rule --name state-machine-rule --schedule-expression "cron(0 0 ? * SAT *)" --state "ENABLED"

# 4. Attach State Machine to rule
aws events put-targets --rule state-machine-rule --targets "Id"="1","Arn"="$statemachine","RoleArn"="$rolearn","Input"='"{}"'
