variable "profile" {
  type        = string
  description = "(Required) Use a specific profile from your credential file."
  default     = "default"
}

variable "region" {
  type        = string
  description = "(Optional) The AWS region to use."
  default     = "us-west-2"
}

variable "namespace" {
  type        = string
  description = "A short abbreviation of the company name."
  default     = null
  validation {
    condition     = var.namespace == null || (length(var.namespace) >= 2 && length(var.namespace) <= 4)
    error_message = "The abbreviation should consist of 2-4 letters."
  }
}

variable "environment" {
  type        = string
  description = "(Required) Environment name."
}

variable "name" {
  type        = string
  description = "(Required) Service name that will be prefixed to resource names."
}

variable "bucket" {
  type        = string
  description = "(Required) S3 bucket name with source codes."
}

variable "terraform_backend" {
  type        = string
  description = "(Required) The name of the S3 bucket where state snapshots are stored."
}

variable "vpc_id" {
  type        = string
  description = "(Required) VPC Id."
}

variable "master_allowed_security_groups" {
  type        = list(string)
  description = "(Optional) List of Security Groups to allow access to master node."
  default     = []
}

variable "master_allowed_cidr_blocks" {
  type        = list(string)
  description = "(Optional) List of CIDR blocks to allow access to master node."
  default     = []
}

variable "slave_allowed_security_groups" {
  type        = list(string)
  description = "(Optional) List of Security Groups to allow access to slave nodes."
  default     = []
}

variable "emr_release_label" {
  type        = string
  description = "(Required) Release label for the Amazon EMR release."
}

variable "slave_allowed_cidr_blocks" {
  type        = list(string)
  description = "(Optional) List of CIDR blocks to allow access to slave nodes."
  default     = []
}

variable "key_name" {
  type        = string
  description = "(Required) Amazon EC2 key pair that can be used to ssh to the master node."
}

variable "public_subnets" {
  type        = list(string)
  description = ""
}

variable "private_subnets" {
  type        = list(string)
  description = ""
}
#
#variable "subnet_id" {
#  type        = string
#  description = "(Required) VPC subnet id where you want the job flow to launch."
#}

variable "master_instance_type" {
  type        = string
  description = "(Required) EC2 instance type for all instances in the master instance group."
}

variable "master_instance_count" {
  type        = number
  description = "(Optional) Target number of instances for the master instance group."
  default     = 1
}

variable "master_bid_price" {
  type        = number
  description = "(Optional) Bid price for each EC2 instance in the instance group."
  default     = null
}

variable "master_ebs_size" {
  type        = number
  description = "(Required) The master instance volume size, in gibibytes (GiB)."
}

variable "master_ebs_type" {
  type        = string
  description = "(Required) The master instance volume type."
  validation {
    condition     = contains(["gp2", "io1", "standard", "st1"], var.master_ebs_type)
    error_message = "Valid options are gp2, io1, standard and st1."
  }
}

variable "master_ebs_iops" {
  type        = number
  description = "(Optional) Number of I/O operations per second (IOPS) that the master instance volume supports."
  default     = null
}

variable "master_ebs_volumes_per_instance" {
  type        = number
  description = "(Optional) Number of EBS volumes with this configuration to attach to each EC2 instance in the master instance group"
  default     = 1
}

variable "core_instance_type" {
  type        = string
  description = "(Required) EC2 instance type for all instances in the core instance group."
}

variable "core_instance_count" {
  type        = number
  description = "(Optional) Target number of instances for the core instance group."
  default     = 1
}

variable "core_bid_price" {
  type        = number
  description = "(Optional) Bid price for each EC2 instance in the core instance group"
  default     = null
}

variable "core_ebs_size" {
  type        = number
  description = "(Required) The core instance volume size, in gibibytes (GiB)."
}

variable "core_ebs_type" {
  type        = string
  description = "(Required) The core instance volume type."
  validation {
    condition     = contains(["gp2", "io1", "standard", "st1"], var.core_ebs_type)
    error_message = "Valid options are gp2, io1, standard and st1."
  }
}

variable "core_ebs_iops" {
  type        = number
  description = "(Optional) Number of I/O operations per second (IOPS) that the core instance volume supports."
  default     = null
}

variable "core_ebs_volumes_per_instance" {
  type        = number
  description = "(Optional) Number of EBS volumes with this configuration to attach to each EC2 instance in the core instance group"
  default     = 1
}

variable "image_repository_url" {
  type        = string
  description = ""
}

variable "image_repository_name" {
  type        = string
  description = ""
}

variable "image_tag" {
  type        = string
  description = ""
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "(Optional) A list of additional tags to apply to resources."
}
