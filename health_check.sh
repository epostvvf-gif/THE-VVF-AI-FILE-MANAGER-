#!/bin/bash
set -e

echo "========================================================="
echo "   VVF Smart File Manager Ultra: Local Gatekeeper check"
echo "========================================================="

echo "Step 1: Environment Check..."
if command -v java >/dev/null 2>&1; then
    echo "  [OK] Java is available: $(java -version 2>&1 | head -n 1)"
else
    echo "  [FAIL] Java is missing!"
    exit 1
fi

if command -v gradle >/dev/null 2>&1; then
    echo "  [OK] Gradle is available: $(gradle -v | grep "Gradle " | head -n 1)"
else
    echo "  [FAIL] Gradle is missing!"
    exit 1
fi

echo "Step 2: Run Lint Checks..."
# Run Gradle lint check to enforce code styling and quality rules
gradle :app:lintDebug

echo "Step 3: Run Unit Tests..."
# Run local JVM unit tests to verify mathematical on-device AI projections
gradle :app:testDebugUnitTest

echo "Step 4: Run Compilation check..."
# Run build assembly to ensure perfect DEX merging and compilation
gradle assembleDebug

echo "========================================================="
echo "  [SUCCESS] All checks passed successfully!"
echo "  Your codebase is secure, lint-clean, fully tested, and compile-safe!"
echo "========================================================="
