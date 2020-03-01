from aws_cdk import core, aws_codebuild, aws_s3


class Qb4jStack(core.Stack):

    def __init__(self, scope: core.Construct, id: str, env: str, **kwargs) -> None:
        super().__init__(scope, id, **kwargs)

        # Codebuild
        codebuild_project = aws_codebuild.Project(self, 'CodeBuildProject',
                                                  project_name='QueryBuilder4JMVC',
                                                  source=aws_codebuild.Source.git_hub(
                                                      owner='jones-chris',
                                                      repo='QueryBuilder4JMVC',
                                                      branch_or_ref='master',
                                                      clone_depth=1),
                                                  badge=True,
                                                  artifacts=aws_codebuild.Artifacts.s3(
                                                      bucket=aws_s3.Bucket(self, 'CodeBuildArtifactBucket',
                                                                           bucket_name='qb4j-mvc',
                                                                           block_public_access=aws_s3.BlockPublicAccess(
                                                                               restrict_public_buckets=True
                                                                           )),
                                                      name=f'querybuilder4jmvc-{env}',
                                                      include_build_id=True,
                                                      path='build/')
                                                  # allow_all_outbound=True  # True so docker image can be sent to docker hub.
                                                  )

        # ECS

        # Define CW alarm with SNS topic?
