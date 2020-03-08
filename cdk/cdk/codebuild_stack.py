from aws_cdk import core, aws_codebuild, aws_s3, aws_iam


class CodeBuildStack(core.Stack):

    def __init__(self, scope: core.Construct, id: str, env: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        # Codebuild project.
        codebuild_project = aws_codebuild.Project(
            self, 'CodeBuildProject',
            project_name='QueryBuilder4JMVC',
            badge=True,
            cache=aws_codebuild.Cache.bucket(
                bucket=aws_s3.Bucket(
                    self, 'CodeBuildDependencyCacheBucket',
                    bucket_name='m2-dependencies',
                    block_public_access=aws_s3.BlockPublicAccess(
                        restrict_public_buckets=True,
                        block_public_policy=True
                    )
                )
            ),
            source=aws_codebuild.Source.git_hub(
                owner='jones-chris',
                repo='QueryBuilder4JMVC',
                branch_or_ref='master',
                clone_depth=1
            ),
            artifacts=aws_codebuild.Artifacts.s3(
                bucket=aws_s3.Bucket(
                    self, 'CodeBuildArtifactBucket',
                    bucket_name='qb4j-mvc',
                    block_public_access=aws_s3.BlockPublicAccess(
                        restrict_public_buckets=True,
                        block_public_policy=True
                    )
                ),
                name=f'querybuilder4jmvc-{env}.jar',
                include_build_id=True,
                path='build/'
            ),
            environment=aws_codebuild.BuildEnvironment(
                privileged=True  # This allows codebuild access to docker in order to build docker images.
            ),
            role=aws_iam.Role(
                self, 'CodeBuildRole',
                assumed_by=aws_iam.ServicePrincipal(
                    service='codebuild.amazonaws.com'
                ),
                inline_policies={
                    'policy_0': aws_iam.PolicyDocument(
                        statements=[
                            aws_iam.PolicyStatement(
                                effect=aws_iam.Effect.ALLOW,
                                actions=[
                                    's3:GetObject',
                                    's3:GetBucket',
                                    's3:List*'
                                ],
                                resources=[
                                    'arn:aws:s3:::maven-build-settings',
                                    'arn:aws:s3:::maven-build-settings/*'
                                ]
                            ),
                            aws_iam.PolicyStatement(
                                effect=aws_iam.Effect.ALLOW,
                                actions=[
                                    'ssm:GetParameters'
                                ],
                                resources=[
                                    '*'
                                ]
                            )
                        ]
                    )
                }
            )
        )

        # Add permissions to S3 bucket that contains maven settings.xml.
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