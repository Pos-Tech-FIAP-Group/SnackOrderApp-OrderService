terraform {
  backend "s3" {
    bucket = "snack-app-artifacts"
    key    = "terraform/state/api/terraform.tfstate"
    region = "sa-east-1"
  }
}
