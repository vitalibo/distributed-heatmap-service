output "master_public_dns" {
  value       = aws_emr_cluster.emr.master_public_dns
  description = "The DNS name of the master node."
}

output "alb_public_dns" {
  value       = aws_alb.alb.dns_name
  description = "The DNS name of the load balancer."
}
