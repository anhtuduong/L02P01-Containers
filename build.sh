# Compile the project
./gradlew clean build
# Build the image
docker build -t eist-ngrok .
# Fire up the containers
docker compose up -d