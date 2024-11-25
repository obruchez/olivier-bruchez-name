#!/bin/bash

# Extract current version from build.sbt
CURRENT_VERSION=$(grep "version :=" build.sbt | sed -E 's/.*"([0-9]+\.[0-9]+\.[0-9]+)".*/\1/')
if [ -z "$CURRENT_VERSION" ]; then
    echo "Error: Could not find version in build.sbt"
    exit 1
fi

# Update version
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"
NEW_PATCH=$((PATCH + 1))
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"
NEW_VERSION="$MAJOR.$MINOR.$NEW_PATCH"

# Update version in build.sbt
if [ -f "build.sbt" ]; then
    # Create backup
    cp build.sbt build.sbt.bak

    # Update version line
    sed -i.tmp "s/version := \".*\"/version := \"$NEW_VERSION\"/" build.sbt

    # Check if the version was actually updated
    if grep -q "version := \"$NEW_VERSION\"" build.sbt; then
        echo "Successfully updated version from $CURRENT_VERSION to $NEW_VERSION"
        rm build.sbt.bak
        rm build.sbt.tmp
    else
        echo "Error: Could not update version. Please check build.sbt format"
        mv build.sbt.bak build.sbt
        exit 1
    fi
else
    echo "Error: build.sbt not found in current directory"
    exit 1
fi

sbt Docker/publishLocal

docker login -u obruchez

docker tag "olivier-bruchez-name:$NEW_VERSION" "obruchez/olivier-bruchez-name:$NEW_VERSION"
docker push "obruchez/olivier-bruchez-name:$NEW_VERSION"

git add build.sbt
git commit -m "Update version to $NEW_VERSION"

echo "Successfully published version $NEW_VERSION, please push the changes to git"
