resource "aws_security_group" "app_sg" {
  name   = "${local.project_name}-app"
  vpc_id = var.vpc_id
}

resource "aws_security_group" "rds_sg" {
  name   = "${local.project_name}-rds"
  vpc_id = var.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
