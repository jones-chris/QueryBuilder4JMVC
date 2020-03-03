from aws_cdk import core, aws_codebuild, aws_s3, aws_iam, aws_ecs, aws_servicediscovery, aws_ec2


class Qb4jStack(core.Stack):

    def __init__(self, scope: core.Construct, id: str, env: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        # # Create cache bucket
        # cache_bucket = aws_s3.Bucket(
        #     self, 'CodeBuildDependencyCacheBucket',
        #     bucket_name='m2-dependencies',
        #     block_public_access=aws_s3.BlockPublicAccess(
        #         restrict_public_buckets=True,
        #         block_public_policy=True
        #     )
        # )
        #
        # # Codebuild project.
        # codebuild_project = aws_codebuild.Project(
        #     self, 'CodeBuildProject',
        #     project_name='QueryBuilder4JMVC',
        #     badge=True,
        #     cache=aws_codebuild.Cache.bucket(
        #         bucket=cache_bucket
        #     ),
        #     source=aws_codebuild.Source.git_hub(
        #         owner='jones-chris',
        #         repo='QueryBuilder4JMVC',
        #         branch_or_ref='master',
        #         clone_depth=1),
        #     artifacts=aws_codebuild.Artifacts.s3(
        #         bucket=aws_s3.Bucket(
        #             self, 'CodeBuildArtifactBucket',
        #             bucket_name='qb4j-mvc',
        #             block_public_access=aws_s3.BlockPublicAccess(
        #                 restrict_public_buckets=True,
        #                 block_public_policy=True
        #             )),
        #         name=f'querybuilder4jmvc-{env}.jar',
        #         include_build_id=True,
        #         path='build/'),
        #     environment=aws_codebuild.BuildEnvironment(
        #         privileged=True  # This allows codebuild access to docker in order to build docker images.
        #     )
        # )
        #
        # # Add permissions to S3 bucket that contains maven settings.xml.
        # codebuild_project.role.add_to_policy(aws_iam.PolicyStatement(
        #     effect=aws_iam.Effect.ALLOW,
        #     actions=[
        #         's3:GetObject',
        #         's3:GetBucket',
        #         's3:List*'
        #     ],
        #     resources=[
        #         'arn:aws:s3:::maven-build-settings',
        #         'arn:aws:s3:::maven-build-settings/*'
        #     ]
        # ))
        #
        # # Add permissions for SSM so they can be pulled in during the CodeBuild builds.
        # codebuild_project.role.add_to_policy(aws_iam.PolicyStatement(
        #     effect=aws_iam.Effect.ALLOW,
        #     actions=[
        #         'ssm:GetParameters'
        #     ],
        #     resources=[
        #         '*'
        #     ]
        # ))

        # ECS
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
        task_definition = aws_ecs.TaskDefinition(
            self, 'TaskDefinition',
            compatibility=aws_ecs.Compatibility.FARGATE,
            cpu='256',
            memory_mib='512',
            network_mode=aws_ecs.NetworkMode.AWS_VPC,
            execution_role=role,
            task_role=role
        )

        # container definition
        container_definition = aws_ecs.ContainerDefinition(
            self, 'ContainerDefinition',
            image=aws_ecs.ContainerImage.from_registry(
                name='joneschris/qb4j-mvc:1'
            ),
            essential=True,
            task_definition=task_definition
            # todo:  add a logging configuration here?
        )
        container_definition.add_port_mappings(aws_ecs.PortMapping(
            container_port=8080,
            host_port=8080,
            protocol=aws_ecs.Protocol.TCP
        ))

        # Vpc
        vpc = aws_ec2.Vpc(
            self, 'Vpc'
        )

        # fargate cluster
        fargate_cluster = aws_ecs.Cluster(
            self, 'FargateCluster',
            cluster_name='qb4j-mvc',
            default_cloud_map_namespace=aws_ecs.CloudMapNamespaceOptions(
                name='querybuilder4j2.net',
                type=aws_servicediscovery.NamespaceType.DNS_PUBLIC,
                vpc=vpc
            ),
            vpc=vpc
        )

        # ecs service
        ecs_service = aws_ecs.FargateService(
            self, 'EcsService',
            task_definition=task_definition,
            # assign_public_ip=True,
            cluster=fargate_cluster
            # vpc_subnets=None  # todo:  do these need to be public subnets?
        )
        ecs_service.enable_cloud_map(
            dns_record_type=aws_servicediscovery.DnsRecordType.A,
            cloud_map_namespace=fargate_cluster.default_cloud_map_namespace
        )

        # load balancer

        # target group

        # Define CW alarm with SNS topic?
