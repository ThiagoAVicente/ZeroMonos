#!/bin/bash

echo "Running load Tests..."
echo ""

# Check if app is running
if ! curl -f http://localhost:8080 > /dev/null 2>&1; then
    echo "Application not running on localhost:8080"
    exit 1
fi

echo "Application is running"
echo ""

# Create results directory
mkdir -p performance-results

# Run tests
echo "Running Load Test..."
k6 run --out json=/dev/null load-testing/base-load-test.js | tee performance-results/load-test.txt
echo ""

echo "Running Stress Test..."
k6 run --out json=/dev/null load-testing/stress-test.js | tee performance-results/stress-test.txt
echo ""

echo "Running Spike Test..."
k6 run --out json=/dev/null load-testing/spike-test.js | tee performance-results/spike-test.txt
echo ""

echo "All tests completed!"
echo "Results saved in performance-results/"
ls -lh performance-results/
