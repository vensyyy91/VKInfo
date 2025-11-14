# VK-Info

Приложение для работы с VK API - получения информации о пользователях и группах.

***
### Функционал
1. Авторизация

| Метод | URL                 | Описание                 |
|-------|---------------------|--------------------------|
| POST  | `/api/auth/login`   | Логин пользователя       |
| POST  | `/api/auth/logout`  | Логаут пользователя      |
| POST  | `/api/auth/refresh` | Обновление access-токена |

Данные для входа по умолчанию (пароль можно поменять в переменных окружения, об этом ниже):
````
username: admin
password: qwerty123
````
2. Работа с VK

| Метод | URL            | Описание                               |
|-------|----------------|----------------------------------------|
| POST  | `/api/vk/info` | Получение информации о пользователе VK |

3. OpenAPI-документация

| Метод | URL                      | Описание                                |
|-------|--------------------------|-----------------------------------------|
| GET   | `/swagger-ui/index.html` | Интерфейс Swagger UI (документация API) |
***

### Требования

Для локального запуска:

* JDK 21+

* Maven 3.9+

* PostgreSQL

Для запуска в контейнерах / облаке:

* Docker

* Minikube

***

### Сборка и запуск
````
git clone https://github.com/vensyyy91/VKInfo.git
````
````
cd VKInfo
````

#### Вариант 1 - Minikube
1. Убедитесь, что Minikube запущен:
````
minikube start
````
2. Запустите скрипт:

На этом этапе при желании можно задать переменные:

POSTGRES_PASSWORD - пароль от БД

JWT_SECRET - секрет для подписи JWT-токенов

ADMIN_PASSWORD - пароль от администратора

Эти переменные можно не задавать, тогда будут использоваться значения по умолчанию

Например:
````
//для всех трех переменных заданы свои значения
POSTGRES_PASSWORD=postgres JWT_SECRET=VG75QcmfGUWwtCDhk7SByBfTeQEdRSk9GHpF6gfyHShepd8t6VvX4HrhTmNn7pbG ADMIN_PASSWORD=mypassword ./k8s/deploy.sh
````
или:
````
//для переменной ADMIN_PASSWORD задано свое значение, 
//для переменных POSTGRES_PASSWORD и JWT_SECRET используются значения по умолчанию
ADMIN_PASSWORD=mypassword ./k8s/deploy.sh
````
или:
````
//для всех трех переменных используются значения по умолчанию
./k8s/deploy.sh
````
3. Запустите приложение:
````
minikube service vk-info-service -n vk-info
````

#### Вариант 2 - Docker
````
docker-compose up
````

#### Вариант 3 - локальный запуск
1. Создайте в корне проекта файл .env со следующими переменными (значения можно подставить свои):
````
//Обязательные
DB_URL=jdbc:postgresql://localhost:5432/vk_info_db?autoReconnect=true
DB_USER=vk_info_user
DB_PASSWORD=qwerty

//Могут быть не указаны (подставятся значения по умолчанию)
SERVER_PORT=8080
JWT_SECRET=VG75QcmfGUWwtCDhk7SByBfTeQEdRSk9GHpF6gfyHShepd8t6VvX4HrhTmNn7pbG
ADMIN_PASSWORD=qwerty123
````
Переменные DB_URL, DB_USER, DB_PASSWORD нужно указать обязательно. Остальные переменные могут быть не указаны — в этом случае будут использованы значения по умолчанию.
2. Создайте БД PostgreSQL с параметрами, которые Вы указали в .env
3. Соберите и запустите приложение:
````
mvn clean spring-boot:run
````