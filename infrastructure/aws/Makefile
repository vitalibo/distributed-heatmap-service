environment := dev
profile := default
auto_approve := false

ENVIRONMENTS := $(shell find ./vars -type f -name '*.tfvars' | cut -c 8- | rev | cut -c 8- | rev | paste -sd " " - | xargs)

define GET_TFVAR
$(shell sed -n 's/^$(1) *= *"\(.*\)"$$/\1/p' vars/$(environment).tfvars)
endef

image := $(shell aws ecr describe-repositories \
    --repository-names "$(call GET_TFVAR,'image_repository_name')" \
    --query 'repositories[0].repositoryUri' \
    --profile $(profile) \
    --output text)
tag := foo

# With JDK 1.8 following error is present when compile heatmap-loader module:
#   Lombok annotation handler class lombok.javac.handlers.HandleDelegate failed on com/github/vitalibo/heatmap/loader/core/Spark.java:
#   com.sun.tools.javac.code.Symbol$CompletionFailure: class file for org.apache.spark.sql.SparkSession$1 not found.
JAVA_HOME := $(shell /usr/libexec/java_home -v 17)

.PHONY: help
help:
	@echo "Usage: make <plan|apply|destroy> environment=<string> profile=<string> auto_approve=<true|false>"
	@echo " "
	@echo "Options:"
	@echo "  environment      The terraform input variables file name (default: '$(environment)'. supported values: [$(ENVIRONMENTS)])."
	@echo "  profile          Use a specific AWS profile from your credential file (default: '$(profile)')."
	@echo "  auto_approve     Skip interactive approval of plan before applying (default: '$(auto_approve)')."
	@echo " "

configure:
    ifeq ($(filter $(environment),$(ENVIRONMENTS)),)
		$(error Environment '$(environment)' is not supported)
    endif
    TF_ENV_VARS := TF_DATA_DIR="$(CURDIR)/.terraform/$(environment)"
    TF_OPTIONS := $(TF_OPTIONS) -var="environment=$(environment)"
    TF_OPTIONS := $(TF_OPTIONS) -var="profile=$(profile)"
    TF_OPTIONS := $(TF_OPTIONS) -var="image_repository_url=$(image)"
    TF_OPTIONS := $(TF_OPTIONS) -var="image_tag=$(tag)"
    TF_INIT_CONF := $(TF_INIT_CONF) -backend-config="bucket=$(call GET_TFVAR,'terraform_backend')"
    TF_INIT_CONF := $(TF_INIT_CONF) -backend-config="key=$(call GET_TFVAR,'name')/$(environment)/$(call GET_TFVAR,'region')/terraform.tfstate"
    TF_INIT_CONF := $(TF_INIT_CONF) -backend-config="profile=$(profile)"
    TF_INIT_CONF := $(TF_INIT_CONF) -backend-config="region=$(call GET_TFVAR,'region')"
    TF_INIT_CONF := $(TF_INIT_CONF) -var-file="$(CURDIR)/vars/$(environment).tfvars"
    TF_RUN_CONF := $(TF_RUN_CONF) -var-file="$(CURDIR)/vars/$(environment).tfvars"
    ifeq ($(auto_approve),true)
        TF_RUN_CONF := $(TF_RUN_CONF) -auto-approve
    endif

.PHONY: init
init:
	$(TF_ENV_VARS) terraform init $(TF_OPTIONS) $(TF_INIT_CONF)

.PHONY: package
package:
	JAVA_HOME=$(JAVA_HOME) mvn clean package -P aws -Dmaven.test.skip=true -f $(CURDIR)/../../pom.xml
	docker build -t "$(image):$(tag)" $(CURDIR)/../../heatmap-api/
	aws ecr get-login-password --profile $(profile) | docker login --username AWS --password-stdin $(image)
	docker push "$(image):$(tag)"
	docker logout

.PHONY: plan
plan: configure init package
	$(TF_ENV_VARS) terraform plan $(TF_OPTIONS) $(TF_RUN_CONF)

.PHONY: apply
apply: configure init package
	$(TF_ENV_VARS) terraform apply $(TF_OPTIONS) $(TF_RUN_CONF)

.PHONY: destroy
destroy: configure init
	$(TF_ENV_VARS) terraform destroy $(TF_OPTIONS) $(TF_RUN_CONF)
