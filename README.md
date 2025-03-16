# Metadata Project

## Опис проекту
Цей проект дозволяє редагувати EXIF-метадані у JPG-файлах. Він складається з:
- **Фронтенду** на React
- **Бекенду** на Spring Boot
- **Бази даних** PostgreSQL для збереження оброблених файлів

## Вимоги
Перед запуском переконайтеся, що у вас встановлені:
- [Node.js](https://nodejs.org/) (для React)
- [Java 17+](https://adoptium.net/) (для Spring Boot)
- [Maven](https://maven.apache.org/) (або використовуйте `mvnw` для Windows/Linux/Mac)
- [PostgreSQL](https://www.postgresql.org/)
- Git Bash (для Windows) або WSL (опціонально)

## Налаштування бази даних
1. Запустіть PostgreSQL.
2. Створіть базу даних:
   ```sql
   CREATE DATABASE metadata_db;
   ```
3. Встановіть змінні оточення у `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/metadata_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

## Запуск проекту
### 1. Запуск через скрипт
Для автоматичного запуску бекенду та фронтенду виконайте у корені проекту:
```bash
chmod +x start.sh  # Для Linux/Mac
./start.sh
```
На Windows використовуйте Git Bash або PowerShell:
```powershell
bash start.sh
```

### 2. Ручний запуск
#### Запуск бекенду
```bash
cd backend
./mvnw spring-boot:run
```
#### Запуск фронтенду
```bash
cd frontend
npm install
npm start
```

## Ліцензія
Цей проект розповсюджується під ліцензією MIT.

