@REM # Shut off all the containers
call docker compose down

@REM # Remove the spring application image
call docker rmi eist-ngrok:latest
@REM # Remove the postgres database image
call docker rmi postgres:latest
@REM # Remove the ngrok image
call docker rmi ngrok/ngrok:latest

@REM # Remove the `/build` directory
rmdir /s /q build
@REM # Remove the `/webapp` directory
rmdir /s /q webapp
