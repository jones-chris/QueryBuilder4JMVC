from aws_cdk import core, aws_iam, aws_ecs, aws_ec2, aws_elasticloadbalancingv2, aws_ecs_patterns


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
                principals=[aws_iam.ServicePrincipal('ecs.amazonaws.com')
            ])
        )

        # task definition
        # task_definition = aws_ecs.TaskDefinition(
        #     self, 'TaskDefinition',
        #     compatibility=aws_ecs.Compatibility.FARGATE,
        #     cpu='256',
        #     memory_mib='512',
        #     network_mode=aws_ecs.NetworkMode.AWS_VPC,
        #     execution_role=role,
        #     task_role=role
        # )

        # container definition
        # container_definition = aws_ecs.ContainerDefinition(
        #     self, 'ContainerDefinition',
        #     image=aws_ecs.ContainerImage.from_registry(
        #         name='joneschris/qb4j-mvc:1'
        #     ),
        #     essential=True,
        #     task_definition=task_definition
        #     # todo:  add a logging configuration here?
        # )
        # container_definition.add_port_mappings(aws_ecs.PortMapping(
        #     container_port=8080,
        #     host_port=8080,
        #     protocol=aws_ecs.Protocol.TCP
        # ))

        vpc = aws_ec2.Vpc(
            self, 'Vpc'
        )

        # fargate cluster
        # fargate_cluster = aws_ecs.Cluster(
        #     self, 'FargateCluster',
        #     cluster_name='qb4j-mvc',
        #     vpc=vpc
        # )

        # ecs service
        # ecs_service = aws_ecs.FargateService(
        #     self, 'EcsService',
        #     task_definition=task_definition,
        #     assign_public_ip=True,  # Expose the container to the public internet.
        #     cluster=fargate_cluster
        # )

        # target group
        # listener = aws_elasticloadbalancingv2.ApplicationListener(
        #     self, 'HttpListener',
        #     load_balancer=aws_elasticloadbalancingv2.ApplicationLoadBalancer(
        #         self, 'ApplicationLoadBalancer',
        #         vpc=vpc,
        #         internet_facing=True,
        #         security_group=vpc.vpc_default_security_group,
        #         vpc_subnets=vpc.public_subnets
        #     ),
        #     open=True,
        #     port=80,  # HTTP
        #     protocol=aws_elasticloadbalancingv2.ApplicationProtocol.HTTP,
        #     default_target_groups=[
        #         aws_elasticloadbalancingv2.ApplicationTargetGroup(
        #             self, 'HttpTargetGroup',
        #             port=80,
        #             protocol=aws_elasticloadbalancingv2.ApplicationProtocol.HTTP,
        #             targets=[
        #                 aws_elasticloadbalancingv2.InstanceTarget(
        #
        #                 )
        #             ]
        #         )
        #     ]
        # )

        load_balanced_fargate_service = aws_ecs_patterns.ApplicationLoadBalancedFargateService(
            self, 'LoadBalancedFargateService',
            assign_public_ip=True,  # todo:  could make this false since the load balancer is public.
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
                # image=aws_ecs.FargateTaskDefinition(
                #     self, 'FargateTaskDefinition',
                #     cpu=256,
                #     memory_limit_mib=512,
                #     execution_role=role,
                #     task_role=role
                # )
            ),
            desired_count=1,
            listener_port=80,
            protocol=aws_elasticloadbalancingv2.ApplicationProtocol.HTTP,
            public_load_balancer=True,
            vpc=vpc
        )

        # Define CW alarm with SNS topic?

        # rds test

