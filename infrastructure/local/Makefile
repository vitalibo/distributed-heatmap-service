package := true

minikube_status := $(shell minikube status | grep host | cut -f 2 -d' ')
tag := $(shell date | md5)

.PHONY: help
help:
	@echo "Usage: make <install|uninstall> package=true|false"
	@echo " "
	@echo "Options:"
	@echo "  package         Enable package maven project (Default: $(package))."
	@echo " "

.PHONY: install
install:
    ifneq ($(minikube_status),Running)
		minikube start --driver=virtualbox --dns-proxy=true
    endif

	minikube update-context

    ifeq ($(package),true)
		eval $$(minikube docker-env) && \
		mvn clean package -f $(CURDIR)/../../pom.xml
    endif

	eval $$(minikube docker-env) && \
	for module in 'plugin' 'api' ; do \
        docker build -t hbase-coprocessor-$$module:$(tag) $(CURDIR)/../../hbase-coprocessor-$$module/ ; \
    done

	helm upgrade -i local . --set hbase.image.tag=$(tag) --set image.tag=$(tag) --wait

.PHONY: uninstall
uninstall:
	helm uninstall local
	kubectl delete pvc data-local-zookeeper-0 dfs-local-hdfs-namenode-0 dfs-local-hdfs-datanode-0
