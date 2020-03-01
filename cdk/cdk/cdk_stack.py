from aws_cdk import core, aws_codebuild, aws_s3, aws_iam


class Qb4jStack(core.Stack):

    def __init__(self, scope: core.Construct, id: str, env: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        # Codebuild project.
        codebuild_project = aws_codebuild.Project(self, 'CodeBuildProject',
                                                  project_name='QueryBuilder4JMVC',
                                                  badge=True,
                                                  source=aws_codebuild.Source.git_hub(
                                                      owner='jones-chris',
                                                      repo='QueryBuilder4JMVC',
                                                      branch_or_ref='master',
                                                      clone_depth=1),
                                                  artifacts=aws_codebuild.Artifacts.s3(
                                                      bucket=aws_s3.Bucket(self, 'CodeBuildArtifactBucket',
                                                                           bucket_name='qb4j-mvc',
                                                                           block_public_access=aws_s3.BlockPublicAccess(
                                                                               restrict_public_buckets=True,
                                                                               block_public_policy=True
                                                                           )),
                                                      name=f'querybuilder4jmvc-{env}.jar',
                                                      include_build_id=True,
                                                      path='build/'),
                                                  environment=aws_codebuild.BuildEnvironment(
                                                      privileged=True  # This allows codebuild to build docker images.
                                                  )
                                                  # allow_all_outbound=True  # True so docker image can be sent to docker hub.
                                                  )

        # Add permissions to S3 bucket that contains maven settings.xml.
        codebuild_project.role.add_to_policy(aws_iam.PolicyStatement(
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
        ))

        # ECS

        # Define CW alarm with SNS topic?
