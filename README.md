## Проект “Облачное хранилище файлов”

Для запуска приложения локально:
1. Запустить docker-compose
```docker-compose up -d```
2. в корне проекта создать .env со следующим содержимым:

- DB_URL=jdbc:postgresql://localhost:5432/db
- DB_USER=db
- DB_PASSWORD=db
- DB=db
- REDIS_HOST=localhost
- REDIS_PORT=6379
- MINIO_ENDPOINT=http://localhost:9000
- MINIO_ROOT_USER=minio
- MINIO_ROOT_PASSWORD=minio
- MINIO_BUCKET=user-files

и добавить данные переменные в переменные окружения.