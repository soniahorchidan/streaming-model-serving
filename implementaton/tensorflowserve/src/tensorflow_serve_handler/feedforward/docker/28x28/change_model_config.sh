# Change version number
sed -i 's/versions:\([0-9]\+\)/versions:'$1'/g' models/models.config
