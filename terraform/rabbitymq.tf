# Security group para o broker
resource "aws_security_group" "rabbitmq_sg" {
  name        = "rabbitmq-sg"
  description = "Security group for Amazon MQ RabbitMQ broker"
  vpc_id      = var.vpc_id

  # AMQP 5671/5672 - ajuste origem conforme sua infra (aqui está liberado geral, só para exemplo)
  ingress {
    from_port   = 5671
    to_port     = 5672
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Management UI 15672 (se habilitado nas configs do broker)
  ingress {
    from_port   = 15672
    to_port     = 15672
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "rabbitmq-sg"
  }
}

# Broker Amazon MQ for RabbitMQ
resource "aws_mq_broker" "snack_app_broker" {
  broker_name = "snack-app-broker"

  engine_type    = "RabbitMQ"
  engine_version = "3.13"

  host_instance_type = "mq.t3.micro"

  deployment_mode = "SINGLE_INSTANCE"

  publicly_accessible = true

  # security_groups = [aws_security_group.rabbitmq_sg.id]
  subnet_ids      = [var.subnet_id1]

  # storage type padrão é EBS; pode configurar storage_type se quiser
  # storage_type = "ebs"

  user {
    username = var.rabbitmq_username
    password = var.rabbitmq_password
  }

  # Criptografia gerenciada pela AWS por padrão
  auto_minor_version_upgrade = true

  tags = {
    Name = "snack-app-broker"
  }
}
