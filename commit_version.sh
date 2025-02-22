#!/usr/bin/env bash
# Copyright (c) Bosch Software Innovations GmbH 2018.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

# Usage: 
# Use ./commit_version.sh <beginning of git comit-id>
# If you want to build the current head directly, use
# "./commit_version.sh $(git rev-parse HEAD)

set -e

git checkout $1
mvn clean install -DskipTests -Dqualifier=-$(git describe --tags | cut -d"-" -f2-)
