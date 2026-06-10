#!/bin/bash
# Setup script for Google AI integration - Linux/Mac

echo "🚀 Setting up Google AI for Scentify Quiz..."
echo ""

# Check if API key is provided
if [ -z "$1" ]; then
    echo "❌ Error: API key not provided"
    echo "Usage: ./setup-google-ai.sh YOUR_API_KEY"
    echo ""
    echo "Steps to get your API key:"
    echo "1. Go to: https://aistudio.google.com/app/apikey"
    echo "2. Click 'Create API Key'"
    echo "3. Copy the key"
    echo "4. Run: ./setup-google-ai.sh YOUR_KEY_HERE"
    exit 1
fi

API_KEY=$1

echo "Setting GOOGLE_AI_API_KEY environment variable..."
export GOOGLE_AI_API_KEY="$API_KEY"

# Add to bashrc/zshrc for persistence
if [ -f ~/.bashrc ]; then
    echo "export GOOGLE_AI_API_KEY=\"$API_KEY\"" >> ~/.bashrc
    echo "✅ Added to ~/.bashrc"
fi

if [ -f ~/.zshrc ]; then
    echo "export GOOGLE_AI_API_KEY=\"$API_KEY\"" >> ~/.zshrc
    echo "✅ Added to ~/.zshrc"
fi

echo ""
echo "✅ Environment variable set!"
echo ""
echo "📦 Building project..."
cd scentify
mvn clean install -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "🚀 Starting application..."
    mvn spring-boot:run
else
    echo "❌ Build failed. Check errors above."
    exit 1
fi
