resource "aws_db_subnet_group" "default" {
  name       = "${local.project_name}-subnet-group"
  subnet_ids = [var.subnet_id1, var.subnet_id2, var.subnet_id3]

  tags = {
    Name = "SnackApp DB subnet group"
  }
}

resource "aws_db_instance" "postgres_dev" {
  allocated_storage      = 20
  engine                 = "postgres"
  engine_version         = "17.6"
  instance_class         = "db.t3.micro"
  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.default.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  multi_az               = false
  publicly_accessible    = false
  skip_final_snapshot    = true
  tags = local.tags_dev
}