# Copyright 2018 - Swiss Data Science Center (SDSC)
# A partnership between École Polytechnique Fédérale de Lausanne (EPFL) and
# Eidgenössische Technische Hochschule Zürich (ETHZ).
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Define docker image tag
DOCKER_REPOSITORY?=renkuhub/
DOCKER_PREFIX:=${DOCKER_REGISTRY}$(DOCKER_REPOSITORY)
DOCKER_IMAGE:=${DOCKER_PREFIX}renku-storage
DOCKER_LABEL?=$(shell git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/^* //')

# ifeq ($(DOCKER_LABEL), master)
# 	DOCKER_LABEL=latest
# endif

GIT_MASTER_HEAD_SHA:=$(shell git rev-parse --short=12 --verify HEAD)

.PHONY: tag
tag: build
	docker tag $(DOCKER_IMAGE):$(GIT_MASTER_HEAD_SHA) $(DOCKER_IMAGE):$(DOCKER_LABEL)
ifeq (${DOCKER_LABEL}, master)
	docker tag $(DOCKER_IMAGE):$(GIT_MASTER_HEAD_SHA) $(DOCKER_IMAGE):latest
endif

.PHONY: push
push: tag login
	docker push $(DOCKER_IMAGE):$(GIT_MASTER_HEAD_SHA)
	docker push $(DOCKER_IMAGE):$(DOCKER_LABEL)
ifeq (${DOCKER_LABEL}, master)
	docker push $(DOCKER_IMAGE):latest
endif

.PHONY: build
build:
	docker build --rm --force-rm -t $(DOCKER_IMAGE):$(GIT_MASTER_HEAD_SHA) .

# For use with automated tools (e.g. Travis CI)
.PHONY: login
login:
	@docker login -u="$(DOCKER_USERNAME)" -p="$(DOCKER_PASSWORD)" $(DOCKER_REGISTRY)
