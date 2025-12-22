variable "region" {
  type    = string
  default = "sa-east-1"
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "rabbitmq_username" {
  type    = string
  default = "root"
}

variable "rabbitmq_password" {
  type      = string
  sensitive = true
}