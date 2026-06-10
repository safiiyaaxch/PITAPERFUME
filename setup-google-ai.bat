@echo off
REM Setup script for Google AI integration - Windows

echo 🚀 Setting up Google AI for Scentify Quiz...
echo.

REM Check if API key is provided
if "%1"=="" (
    echo ❌ Error: API key not provided
    echo Usage: setup-google-ai.bat YOUR_API_KEY
    echo.
    echo Steps to get your API key:
    echo 1. Go to: https://aistudio.google.com/app/apikey
    echo 2. Click 'Create API Key'
    echo 3. Copy the key
    echo 4. Run: setup-google-ai.bat YOUR_KEY_HERE
    pause
    exit /b 1
)

set API_KEY=%1

echo Setting GOOGLE_AI_API_KEY environment variable...
setx GOOGLE_AI_API_KEY "%API_KEY%"

echo.
echo ✅ Environment variable set!
echo ⚠️  Please restart your terminal/IDE for the change to take effect.
echo.
echo Next steps:
echo 1. Close this terminal window
echo 2. Open a new terminal
echo 3. Run: cd scentify
echo 4. Run: mvn clean install
echo 5. Run: mvn spring-boot:run
echo.
pause
