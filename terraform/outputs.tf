output "rds_endpoint" {
  value       = aws_db_instance.postgres_dev.endpoint
  description = "Endpoint do banco de dados RDS"
}

output "rds_instance_id" {
  value       = aws_db_instance.postgres_dev.id
  description = "ID da instância do banco de dados RDS"
}