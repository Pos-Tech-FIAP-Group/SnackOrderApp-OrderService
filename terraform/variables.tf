variable "region" {
  type    = string
  default = "sa-east-1"
}

variable "vpc_id" {
  type = string
}

variable "subnet_id1" {
  type = string
}

variable "subnet_id2" {
  type = string
}

variable "subnet_id3" {
  type = string
}

variable "rabbitmq_username" {
  type    = string
  default = "root"
}

variable "rabbitmq_password" {
  type      = string
  sensitive = true
}

variable "db_name" {
  type    = string
}

variable "db_username" {
  type      = string
}

variable "db_password" {
  type      = string
  sensitive = true
}