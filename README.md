Supporting AWS Roles in Hadoop
==============================

Currently "hdfs dfs -lsr s3://..." supports access-keys/secret-keys only as the way to authenticate to AWS S3. 

This should support AWS-roles also because of the following reasons :

 1. AWS-roles is a AWS best-practice and is highly recommended by AWS themselves.
 2. This helps in cross-AWS-account integration also. An AWS-account-holder can provide another AWS-account-holder a cross-account-AWS-role to perform operations over his S3-buckets.

The current authentication mechanism supported is :

    hdfs dfs -Dfs.s3n.awsAccessKeyId=XXXX -Dfs.s3n.awsSecretAccessKey=XXXX -ls s3n://.../

This can be changed to support roles like :

    hdfs dfs -Dfs.s3n.awsAccessKeyId=XXXX -Dfs.s3n.awsSecretAccessKey=XXXX -Dfs.s3n.awsRoleToBeAssumed=arn:aws:iam::XXXX:role/XXXX -Dfs.s3n.awsExternalId=XXXX -ls s3n://.../

Extending the use-case a little further, for a client AWS-account to integrate with multiple different AWS-accounts, 
configuration for s3-bucket to role-to-be-assumed mapping ( which will override the master-role ) can be provided :

    hdfs dfs -Dfs.s3.awsAccessKeyId=XXXX -Dfs.s3.awsSecretAccessKey=XXXX -Dfs.s3.awsRoleToBeAssumed=arn:aws:iam::XXXX:role/XXXX -Dfs.s3.awsBucketToRoleMapping="{\"bucket1\": { \"roleName\":\"arn:aws:iam::XXXX:role/role1\", \"externalId\":\"....\"}}" -ls s3://.../

Since, AWS treats a cross-account-AWS-role the same as an AWS-role within a AWS-account, the above flows remain same for a role within a AWS-account.

Architecture / Design
---------------------

 * [Securely sharing data across Organizations with Qubole][qubole-blog]

[qubole-blog]:http://www.qubole.com/securely-sharing-data/

Glossary
--------

 * [Apache Hadoop JIRA][hadoop-jira] 
 * [Apache Hadoop patch][hadoop-patch]
 * [Github commit for JetS3t][jets3t-github-commit] 
 * [Jets3t patch][jets3t-patch]
 
[jets3t-github-commit]: https://github.com/guptavishal/jets3t-aws-roles/commit/61a02cbcd2312710df8ecfe3307348570e8d3607
[hadoop-jira]: https://issues.apache.org/jira/browse/HADOOP-11038
[hadoop-patch]: https://gist.github.com/guptavishal/23fd08d9814763e0af32
[jets3t-patch]: https://gist.github.com/guptavishal/91dcc32f2742a7cc2fb6
