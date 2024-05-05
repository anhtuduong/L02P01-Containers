# Shut off all the containers
docker compose down
# Remove the spring application image
docker rmi eist-ngrok:latest
# Remove the postgres database image
docker rmi postgres:latest
# Remove the ngrok image
docker rmi ngrok/ngrok:latest

# Remove the `/build` directory
rm -r build/
# Remove the `/webapp` directory
sudo rm -rf webapp/