package := true

minikube_status := $(shell minikube status | grep host | cut -f 2 -d' ')
tag := $(shell date | md5)

# With JDK 1.8 following error is present when compile heatmap-loader module:
#   Lombok annotation handler class lombok.javac.handlers.HandleDelegate failed on com/github/vitalibo/heatmap/loader/core/Spark.java:
#   com.sun.tools.javac.code.Symbol$CompletionFailure: class file for org.apache.spark.sql.SparkSession$1 not found.
JAVA_HOME := $(shell /usr/libexec/java_home -v 17)

.PHONY: help
help:
	@echo "Usage: make <up|down> package=true|false"
	@echo " "
	@echo "Options:"
	@echo "  package         Enable package maven project (Default: $(package))."
	@echo " "

.PHONY: setup
setup:
    ifneq ($(minikube_status),Running)
		minikube start --driver=hyperkit
    endif

	minikube update-context

.PHONY: delete
delete:
	minikube delete

.PHONY: configure
configure:
    MODULES := api hbase
    ifeq ($(MAKECMDGOALS),generate)
        MODULES := loader
    endif

    __space := $(subst ,, )
    __comma := ,

.PHONY: package
package: configure
    ifeq ($(package),true)
		@eval $$(minikube docker-env) && JAVA_HOME=$(JAVA_HOME) \
		mvn clean package -pl $(subst $(__space),$(__comma),$(foreach __module,$(MODULES),heatmap-$(__module))) -am \
			-P local -Dmaven.test.skip=true -f $(CURDIR)/../../pom.xml
    endif

	@eval $$(minikube docker-env) && \
	for module in $(MODULES) ; do \
        docker build -t heatmap-$$module:$(tag) $(CURDIR)/../../heatmap-$$module/ ; \
    done

.PHONY: up
up: setup package
	helm upgrade -i local . --set hbase.image.tag=$(tag) --set image.tag=$(tag) --wait

.PHONY: generate
generate: setup package
	helm upgrade -i local . --reuse-values --set spark.image.tag=$(tag) --set spark.enabled=true --wait

.PHONY: down
down:
	helm uninstall local
	kubectl delete pvc data-local-zookeeper-0 dfs-local-hdfs-namenode-0 dfs-local-hdfs-datanode-0
