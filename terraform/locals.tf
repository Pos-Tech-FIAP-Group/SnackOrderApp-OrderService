locals {
  project_name = "snack-order-app"
  tags_dev = {
    Name        = "dev-snackapp"
    Environment = "Develop"
  }
  tags_prod = {
    Name        = "prod-snackapp"
    Environment = "Production"
  }
}
