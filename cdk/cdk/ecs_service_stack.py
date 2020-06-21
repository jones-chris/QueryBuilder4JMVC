from aws_cdk import core, aws_iam, aws_ecs, aws_ec2, aws_elasticloadbalancingv2, aws_ecs_patterns, aws_apigateway


class EcsServiceStack(core.Stack):

    def __init__(self, scope: core.Construct, id: str, env: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        # execution role
        role = aws_iam.Role(
            self, 'TaskDefinitionExecutionRole',
            assumed_by=aws_iam.ServicePrincipal(service='ecs-tasks.amazonaws.com'),
            managed_policies=[
                aws_iam.ManagedPolicy.from_aws_managed_policy_name(
                    managed_policy_name='service-role/AmazonECSTaskExecutionRolePolicy'
                )
            ],
            inline_policies={
                'policy_0': aws_iam.PolicyDocument(  # todo:  make the key f'{env}-{project_name}'
                    statements=[
                        aws_iam.PolicyStatement(
                            effect=aws_iam.Effect.ALLOW,
                            actions=[
                                'ec2:CreateNetworkInterface',
                                'ec2:DescribeNetworkInterfaces',
                                'ec2:DeleteNetworkInterface',
                                'elasticloadbalancing:DeregisterInstancesFromLoadBalancer',
                                'elasticloadbalancing:DeregisterTargets',
                                'elasticloadbalancing:Describe*',
                                'elasticloadbalancing:RegisterInstancesWithLoadBalancer',
                                'elasticloadbalancing:RegisterTargets',
                                'ec2:Describe*',
                                'ec2:AuthorizeSecurityGroupIngress'
                            ],
                            resources=[
                                '*'
                            ]
                        )
                    ]
                )
            }
        )
        role.assume_role_policy.add_statements(
            aws_iam.PolicyStatement(
                actions=[
                    'sts:AssumeRole'
                ],
                principals=[
                    aws_iam.ServicePrincipal('ecs.amazonaws.com')
                ]
            )
        )

        app_load_balanced_ecs_fargate_service = aws_ecs_patterns.ApplicationLoadBalancedFargateService(
            self, 'LoadBalancedFargateService',
            assign_public_ip=False,  # todo:  could make this false since the load balancer is public.
            cpu=256,
            memory_limit_mib=512,
            task_image_options=aws_ecs_patterns.ApplicationLoadBalancedTaskImageOptions(
                container_port=8080,
                enable_logging=True,
                execution_role=role,
                task_role=role,
                image=aws_ecs.RepositoryImage(
                    image_name='joneschris/qb4j-mvc:1'
                ),
            ),
            desired_count=1,
            listener_port=80,
            protocol=aws_elasticloadbalancingv2.ApplicationProtocol.HTTP,
            public_load_balancer=True,
            vpc=aws_ec2.Vpc(
                self, 'Vpc'
            )
        )

        # Define CW alarm with SNS topic?

