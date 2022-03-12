#!/usr/bin/env bash

#sed -i 's/tensorflow_intra_op_parallelism=\([0-9]\+\)/tensorflow_intra_op_parallelism='$1'/g' docker-compose.yml
sed -i 's/tensorflow_inter_op_parallelism=\([0-9]\+\)/tensorflow_inter_op_parallelism='$2'/g' docker-compose.yml
echo "Changed"
