minikube_status := $(shell minikube status | grep host | cut -f 2 -d' ')
tag := $(shell date | md5)

.PHONY: help
help:
	@echo "Usage: make <install|port_forward|uninstall>"
	@echo " "
	@echo "Options:"
	@echo " "

.PHONY: package
package:
	mvn clean package -f $(CURDIR)/../../pom.xml

.PHONY: install
install: package
    ifneq ($(minikube_status),Running)
		minikube start
    endif

	minikube update-context
	eval $$(minikube docker-env) && \
	docker build -t hbase-coprocessor-plugin:$(tag) $(CURDIR)/../../hbase-coprocessor-plugin/

	helm repo add bigdata-gradiant https://gradiant.github.io/bigdata-charts/
	helm upgrade -i hbase bigdata-gradiant/hbase -f values.yaml --set image.tag=$(tag) --wait

.PHONY: port_forward
port_forward:
	@echo 'open http://localhost:16010'
	kubectl port-forward -n default svc/hbase-hbase-master 16010:16010

.PHONY: uninstall
uninstall:
	helm uninstall hbase
	kubectl delete pvc data-hbase-zookeeper-0 dfs-hbase-hdfs-namenode-0 dfs-hbase-hdfs-datanode-{0..2}
