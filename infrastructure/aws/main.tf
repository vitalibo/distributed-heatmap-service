locals {
  full_name = "${var.namespace}${length(var.namespace) > 0 ? "-" : ""}${var.environment}-${var.name}"
  tags      = merge({
    Terraform   = "true"
    Environment = var.environment
  }, var.tags)
}


data "aws_caller_identity" "current" {}

data "aws_region" "current" {}


data "aws_iam_policy_document" "emr_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["elasticmapreduce.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "emr_service_role" {
  name               = "${local.full_name}-emr-role"
  assume_role_policy = data.aws_iam_policy_document.emr_assume_role.json
  tags               = local.tags
}

resource "aws_iam_role_policy_attachment" "emr_service_role" {
  role       = aws_iam_role.emr_service_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceRole"
}

data "aws_iam_policy_document" "ec2_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "emr_ec2_role" {
  name               = "${local.full_name}-emr-ec2-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume_role.json
  tags               = local.tags
}

resource "aws_iam_role_policy_attachment" "emr_ec2_role" {
  role       = aws_iam_role.emr_ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceforEC2Role"
}

resource "aws_iam_instance_profile" "emr_ec2_instance_profile" {
  name = "${local.full_name}-emr-ec2-instance-profile"
  role = aws_iam_role.emr_ec2_role.name
  tags = local.tags
}


resource "aws_security_group" "master" {
  revoke_rules_on_delete = true
  name                   = "${local.full_name}-master"
  vpc_id                 = var.vpc_id
  description            = "Allow inbound traffic and outbound traffic for EMR cluster master node."
  tags                   = local.tags
}

resource "aws_security_group_rule" "master_ingress_security_groups" {
  count                    = length(var.master_allowed_security_groups)
  type                     = "ingress"
  description              = "Allow all inbound traffic from Security Groups."
  from_port                = 0
  to_port                  = 65535
  protocol                 = "tcp"
  source_security_group_id = var.master_allowed_security_groups[count.index]
  security_group_id        = aws_security_group.master.id
}

resource "aws_security_group_rule" "master_ingress_security_groups1" {
  type                     = "ingress"
  description              = "Allow all inbound traffic from Security Groups."
  from_port                = 0
  to_port                  = 65535
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.api_sg.id
  security_group_id        = aws_security_group.master.id
}

resource "aws_security_group_rule" "master_ingress_cidr_blocks" {
  count             = length(var.master_allowed_cidr_blocks) > 0 ? 1 : 0
  description       = "Allow all inbound traffic from CIDR blocks."
  type              = "ingress"
  from_port         = 0
  to_port           = 65535
  protocol          = "tcp"
  cidr_blocks       = var.master_allowed_cidr_blocks
  security_group_id = aws_security_group.master.id
}

resource "aws_security_group_rule" "master_egress" {
  description       = "Allow all egress traffic."
  type              = "egress"
  from_port         = 0
  to_port           = 65535
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  ipv6_cidr_blocks  = ["::/0"]
  security_group_id = aws_security_group.master.id
}

resource "aws_security_group" "slave" {
  revoke_rules_on_delete = true
  name                   = "${local.full_name}-slave"
  vpc_id                 = var.vpc_id
  description            = "Allow inbound traffic and outbound traffic for EMR cluster slave node."
  tags                   = local.tags
}

resource "aws_security_group_rule" "slave_ingress_security_groups" {
  count                    = length(var.slave_allowed_security_groups)
  type                     = "ingress"
  description              = "Allow all inbound traffic from Security Groups."
  from_port                = 0
  to_port                  = 65535
  protocol                 = "tcp"
  source_security_group_id = var.slave_allowed_security_groups[count.index]
  security_group_id        = aws_security_group.slave.id
}

resource "aws_security_group_rule" "slave_ingress_security_groups2" {
  type                     = "ingress"
  description              = "Allow all inbound traffic from Security Groups."
  from_port                = 0
  to_port                  = 65535
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.api_sg.id
  security_group_id        = aws_security_group.slave.id
}

resource "aws_security_group_rule" "slave_ingress_cidr_blocks" {
  count             = length(var.slave_allowed_cidr_blocks) > 0 ? 1 : 0
  description       = "Allow all inbound traffic from CIDR blocks."
  type              = "ingress"
  from_port         = 0
  to_port           = 65535
  protocol          = "tcp"
  cidr_blocks       = var.slave_allowed_cidr_blocks
  security_group_id = aws_security_group.slave.id
}

resource "aws_security_group_rule" "slave_egress" {
  description       = "Allow all egress traffic."
  type              = "egress"
  from_port         = 0
  to_port           = 65535
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.slave.id
}


resource "aws_s3_object" "heatmap_hbase_jar" {
  bucket = var.bucket
  key    = "${var.environment}/${var.name}/heatmap-hbase-1.0-SNAPSHOT.jar"
  source = "${path.module}/../../heatmap-hbase/target/heatmap-hbase-1.0-SNAPSHOT.jar"
}

resource "aws_s3_object" "heatmap_loader_jar" {
  bucket = var.bucket
  key    = "${var.environment}/${var.name}/heatmap-loader-1.0-SNAPSHOT.jar"
  source = "${path.module}/../../heatmap-loader/target/heatmap-loader-1.0-SNAPSHOT.jar"
}

resource "aws_s3_object" "bootstrap_script" {
  bucket  = var.bucket
  key     = "${var.environment}/${var.name}/bootstrap.sh"
  content = <<EOT
#!/bin/sh
set -e

sudo aws s3 cp "s3://${aws_s3_object.heatmap_hbase_jar.bucket}/${aws_s3_object.heatmap_hbase_jar.key}" "/usr/lib/hbase/lib/"
aws s3 cp "s3://${aws_s3_object.heatmap_loader_jar.bucket}/${aws_s3_object.heatmap_loader_jar.key}" "/home/hadoop/"
EOT
}

resource "aws_emr_cluster" "this" {
  name          = "${local.full_name}-cluster"
  release_label = var.emr_release_label
  service_role  = aws_iam_role.emr_service_role.id
  applications  = ["Spark", "HBase", "Livy"]
  log_uri       = "s3://${var.bucket}/${var.environment}/${var.name}/emr_logs/"
  tags          = local.tags

  ec2_attributes {
    key_name                          = var.key_name
    subnet_id                         = var.public_subnets[1]
    emr_managed_master_security_group = aws_security_group.master.id
    emr_managed_slave_security_group  = aws_security_group.slave.id
    instance_profile                  = aws_iam_instance_profile.emr_ec2_instance_profile.arn
  }

  master_instance_group {
    name           = "master-group"
    instance_type  = var.master_instance_type
    instance_count = var.master_instance_count
    bid_price      = var.master_bid_price

    ebs_config {
      size                 = var.master_ebs_size
      type                 = var.master_ebs_type
      iops                 = var.master_ebs_iops
      volumes_per_instance = var.master_ebs_volumes_per_instance
    }
  }

  core_instance_group {
    name           = "core-group"
    instance_type  = var.core_instance_type
    instance_count = var.core_instance_count
    bid_price      = var.core_bid_price

    ebs_config {
      size                 = var.core_ebs_size
      type                 = var.core_ebs_type
      iops                 = var.core_ebs_iops
      volumes_per_instance = var.core_ebs_volumes_per_instance
    }
  }

  configurations_json = jsonencode([
    {
      Classification = "hbase-site"
      Properties     = {
        "hbase.table.sanity.checks" = "false"
      }
    }, {
      Classification = "yarn-site"
      Properties     = {
        "yarn.resourcemanager.scheduler.class" = "org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler"
      }
    }, {
      Classification = "capacity-scheduler"
      Properties     = {
        "yarn.scheduler.capacity.resource-calculator" = "org.apache.hadoop.yarn.util.resource.DominantResourceCalculator"
      }
    }, {
      Classification = "spark-hive-site"
      Properties     = {
        "hive.metastore.client.factory.class" = "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory"
      }
    }, {
      Classification = "livy-conf"
      Properties     = {
        "livy.spark.deploy-mode"     = "cluster"
        "livy.impersonation.enabled" = "true"
      }
    }, {
      Classification = "core-site"
      Properties     = {
        "hadoop.proxyuser.livy.groups" = "*"
        "hadoop.proxyuser.livy.hosts"  = "*"
      }
    }
  ])

  step {
    action_on_failure = "CANCEL_AND_WAIT"
    name              = "Setup Hadoop Debugging"

    hadoop_jar_step {
      jar  = "command-runner.jar"
      args = ["state-pusher-script"]
    }
  }

  bootstrap_action {
    name = "Copy heatmap-hbase jar from S3"
    path = "s3://${aws_s3_object.bootstrap_script.bucket}/${aws_s3_object.bootstrap_script.key}"
  }
}


resource "aws_ecs_cluster" "api" {
  name = "${local.full_name}-api-cluster"
  tags = local.tags

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_ecs_cluster_capacity_providers" "api_capacity_providers" {
  cluster_name       = aws_ecs_cluster.api.name
  capacity_providers = ["FARGATE_SPOT", "FARGATE"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
  }
}

resource "aws_ecs_task_definition" "api_task_definition" {
  family                   = "${local.full_name}-api"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.api_role.arn
  task_role_arn            = aws_iam_role.api_role.arn
  cpu                      = 1024
  memory                   = 2048
  tags                     = local.tags

  container_definitions = jsonencode([
    {
      name         = "heatmap-api"
      essential    = true
      image        = "${var.image_repository_url}:${var.image_tag}"
      memory       = 2048
      portMappings = [
        {
          containerPort = 8080
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options   = {
          "awslogs-group"  = aws_cloudwatch_log_group.api_log_group.name
          "awslogs-region" = var.region
          "awslogs-stream-prefix" : "ecs"
        }
      }
      environment = [
        {
          "name" = "HBASE_ZOOKEEPER_QUORUM", "value" = aws_emr_cluster.this.master_public_dns
        }
      ]
    }
  ])
}

resource "aws_ecs_service" "api_service" {
  name            = "${local.full_name}-api"
  cluster         = aws_ecs_cluster.api.id
  task_definition = aws_ecs_task_definition.api_task_definition.arn
  desired_count   = 1
  launch_type     = "FARGATE"
  propagate_tags  = "TASK_DEFINITION"
  tags            = local.tags

  network_configuration {
    subnets          = var.private_subnets
    security_groups  = [aws_security_group.api_sg.id]
    assign_public_ip = false
  }

  load_balancer {
    container_name   = "heatmap-api"
    container_port   = 8080
    target_group_arn = aws_alb_target_group.alb_target_group.arn
  }
}

resource "aws_cloudwatch_log_group" "api_log_group" {
  name              = "/aws/ecs/${local.full_name}"
  retention_in_days = 7
  tags              = var.tags
}

resource "aws_security_group" "api_sg" {
  name                   = "${local.full_name}-api"
  vpc_id                 = var.vpc_id
  description            = "Allow inbound and outbound traffic for ECS Fargate service."
  revoke_rules_on_delete = true
  tags                   = local.tags

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "api_sg_ingress" {
  description              = "Allow inbound traffic from ALB Security Group."
  type                     = "ingress"
  protocol                 = "tcp"
  from_port                = 8080
  to_port                  = 8080
  source_security_group_id = aws_security_group.alb_sg.id
  security_group_id        = aws_security_group.api_sg.id
}

resource "aws_security_group_rule" "api_sg_egress" {
  description       = "Allow all outbound traffic."
  type              = "egress"
  protocol          = "-1"
  from_port         = 0
  to_port           = 65535
  cidr_blocks       = ["0.0.0.0/0"]
  ipv6_cidr_blocks  = ["::/0"]
  security_group_id = aws_security_group.api_sg.id
}

data "aws_iam_policy_document" "api_assume_role_policy_document" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "api_role" {
  name               = "${local.full_name}-api-role"
  assume_role_policy = data.aws_iam_policy_document.api_assume_role_policy_document.json
  tags               = var.tags
}

data "aws_iam_policy_document" "api_policy_document" {
  statement {
    effect = "Allow"

    resources = [
      "*",
    ]

    actions = [
      "*"
    ]
  }
}

resource "aws_iam_role_policy" "api_policy" {
  name   = "${local.full_name}-api-policy"
  role   = aws_iam_role.api_role.id
  policy = data.aws_iam_policy_document.api_policy_document.json
}


resource "aws_alb" "alb" {
  name               = "${local.full_name}-api"
  load_balancer_type = "application"
  internal           = false
  security_groups    = [aws_security_group.alb_sg.id]
  subnets            = var.public_subnets
  ip_address_type    = "ipv4"
  tags               = local.tags
}

resource "aws_alb_listener" "alb_listener" {
  load_balancer_arn = aws_alb.alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "application/json"
      status_code  = "404"
    }
  }
}

resource "aws_alb_target_group" "alb_target_group" {
  vpc_id               = var.vpc_id
  protocol             = "HTTP"
  target_type          = "ip"
  port                 = 80
  deregistration_delay = 60
  slow_start           = 60
  tags                 = local.tags

  health_check {
    enabled           = true
    interval          = 30
    path              = "/actuator/health"
    healthy_threshold = 2
    protocol          = "HTTP"
    matcher           = "200-299"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_alb_listener_rule" "alb_listener_rule" {
  listener_arn = aws_alb_listener.alb_listener.arn

  condition {
    path_pattern {
      values = ["/v1/*"]
    }
  }

  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.alb_target_group.arn
  }
}

resource "aws_security_group" "alb_sg" {
  name                   = "${local.full_name}-alb"
  vpc_id                 = var.vpc_id
  description            = "Allow inbound and outbound traffic for Application Load Balancer."
  revoke_rules_on_delete = true
  tags                   = local.tags

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "alb_sg_ingress" {
  description       = "Allow all inbound traffic."
  type              = "ingress"
  protocol          = "tcp"
  from_port         = 0
  to_port           = 65535
  cidr_blocks       = ["0.0.0.0/0"]
  ipv6_cidr_blocks  = ["::/0"]
  security_group_id = aws_security_group.alb_sg.id
}

resource "aws_security_group_rule" "alb_sg_egress" {
  description       = "Allow all outbound traffic."
  type              = "egress"
  protocol          = "tcp"
  from_port         = 0
  to_port           = 65535
  cidr_blocks       = ["0.0.0.0/0"]
  ipv6_cidr_blocks  = ["::/0"]
  security_group_id = aws_security_group.alb_sg.id
}
