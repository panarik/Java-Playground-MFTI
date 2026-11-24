# Семинары

## Общее
- решаем какую-то задачку
- коммитим в свою репу
- делаем МРы
- присылаем на ревью
- получаем ревью

## Семинар №1
Примеры в пакете battle. Практическая работа в классе Start

## Семинар №2
Описано тут: mfti/lessons/trycatch/README.md

## Семинар по спрингу

Ветка `spring-1`. Код обоих серверов в source root IDEA, пакет `mfti.lessons.springPatterns`. Подробный разбор по строкам — в `src/main/java/mfti/lessons/springPatterns/beans/README.md`. Java 21. Оба сервера запускаются из корневого `pom.xml`.

Примеры добавлены, чтобы на одних и тех же телах запроса показать две сборки задачи. Ниже каждый сервер со своим запуском и своими запросами. Порты разные, процессы можно держать одновременно.

Общее для запросов: `POST /order`, тело — обычный текст, не JSON. В Postman: Import → Raw text. `Content-Length` можно не считать, если собирать запрос в интерфейсе. Ниже длины уже посчитаны для UTF-8.

### Сервер Spring Boot

Пакет `mfti.lessons.springPatterns.beans` и вложенные `controller`, `service`, `notification`, `department`, `demo`. Контейнер создаёт бины и сам вызывает код. `OrderService` принимает в конструкторе интерфейс `Notifier`, конкретный `EmailNotifier` не импортирует. Книжный и строительный отделы — отдельные бины `BookDepartment` и `ConstructionDepartment`. Контроллер слов «книга» и «бульдозер» не содержит: он обходит список бинов `Department`. На том же старте `SharedCounter` показывает, что singleton-бин один на все потоки.

Команды запускать из корня репозитория. В приглашении оболочки должно быть `Java-Playground-MFTI`, не `beans`. Папка `beans` — это пакет с классами, в ней нет `pom.xml`. Если терминал открыт там, Spring не стартует с ошибкой `No plugin found for prefix 'spring-boot'`. Это не отсутствие бина. Сначала выйти в корень:

```bash
cd "$(git rev-parse --show-toplevel)"
```

Компиляция. Spring при этом не стартует и бины не проверяет. Успех выглядит как `BUILD SUCCESS` в конце лога. Если снять `@Component` с `EmailNotifier`, эта команда всё равно заканчивается `BUILD SUCCESS`: `OrderService` ссылается на интерфейс `Notifier`, а не на класс письма.

```bash
mvn -DskipTests compile
```

Старт контейнера. Процесс не останавливать, пока идут запросы. Без `@Component` на `EmailNotifier` упадёт уже эта команда, хотя компиляция выше прошла.

```bash
mvn spring-boot:run
```

Класс с `main`: `mfti.lessons.springPatterns.beans.BeansApplication`. Порт 8081. Дождаться `Started BeansApplication`. До этой строки в логе пройдёт сценарий бинов: одинаковый номер объекта у `EmailNotifier` и у аргумента `OrderService`, восемь потоков с одним `SharedCounter@...`, без синхронизации меньше 160, с синхронизацией ровно 160.

#### Запрос книги

Вызывается только `BookDepartment`. Ответ: `принят в книжный: книга`. В логе одно письмо.

```http
POST /order HTTP/1.1
Host: localhost:8081
Content-Type: text/plain; charset=utf-8
Content-Length: 10

книга
```

```bash
curl -H 'Content-Type: text/plain; charset=utf-8' -d 'книга' http://localhost:8081/order
```

#### Запрос бульдозера

Вызывается только `ConstructionDepartment`. Ответ: `принят на стройку: бульдозер`. В логе одно письмо.

```http
POST /order HTTP/1.1
Host: localhost:8081
Content-Type: text/plain; charset=utf-8
Content-Length: 18

бульдозер
```

```bash
curl -H 'Content-Type: text/plain; charset=utf-8' -d 'бульдозер' http://localhost:8081/order
```

#### Запрос книги и бульдозера

Подходят оба бина, контроллер никого не выбрасывает. В ответе две строки, в логе два письма.

```text
принят в книжный: книга про бульдозер
принят на стройку: книга про бульдозер
```

```http
POST /order HTTP/1.1
Host: localhost:8081
Content-Type: text/plain; charset=utf-8
Content-Length: 36

книга про бульдозер
```

```bash
curl -H 'Content-Type: text/plain; charset=utf-8' -d 'книга про бульдозер' http://localhost:8081/order
```

### Plain server

Пакет `mfti.lessons.springPatterns.plain`. `PlainOrderServer` только поднимает сервер и отдаёт ему объект `OrderHandler`. Оба `if` лежат в `src/main/java/mfti/lessons/springPatterns/plain/OrderHandler.java`: оба отдела пишут в одну переменную `reply`. Это не Spring: сервер из `com.sun.net.httpserver`.

Команды из корня репозитория. Процесс не останавливать, пока идут запросы.

```bash
mvn compile exec:java@plain-server
```

Порт 8080. В консоли: `ждём POST http://localhost:8080/order`.

#### Запрос книги

Срабатывает только первый `if`. Ответ: `принят в книжный: книга`. В консоли одно письмо: `письмо: книжный отдел: книга`.

```http
POST /order HTTP/1.1
Host: localhost:8080
Content-Type: text/plain; charset=utf-8
Content-Length: 10

книга
```

```bash
curl -d 'книга' http://localhost:8080/order
```

#### Запрос бульдозера

Первый `if` молчит, второй пишет ответ. Ответ: `принят на стройку: бульдозер`. В консоли одно письмо: `письмо: строительный отдел: бульдозер`.

```http
POST /order HTTP/1.1
Host: localhost:8080
Content-Type: text/plain; charset=utf-8
Content-Length: 18

бульдозер
```

```bash
curl -d 'бульдозер' http://localhost:8080/order
```

#### Запрос книги и бульдозера

Истинны оба `if`, писем два. Клиенту уходит только последняя запись в общую `reply`: `принят на стройку: книга про бульдозер`. На Spring Boot в этом месте в ответе две строки. Здесь книжный отдел своё условие не менял: его ответ стёрла ветка, которую дописали ниже в том же методе.

```http
POST /order HTTP/1.1
Host: localhost:8080
Content-Type: text/plain; charset=utf-8
Content-Length: 36

книга про бульдозер
```

```bash
curl -d 'книга про бульдозер' http://localhost:8080/order
```

# Требования МРа:
1. Мейн класс с запуском проекта
2. Логи 


# Что чаще всего спрашивают на собесе
Буду обновлять список темами которые проходим.

1. Основа
- принципы программирования
 - SOLID
- паттерны программирования
 - Какие паттерны Вы используете?

2. ООП
- Чем отличается абстрактный класс от интерфейса
- Какие методы класса Object вы знаете. Далее идут по hash методу глубже.

3. Эксепшены
 - какие эксепшены используете
 - Как обрабатывают эксепшены
 - блок finally {}
 - try catch с ресурсами try () {}

4. Коллекции
- Какими коллекциями вы пользуетесь
- Чем отличается List от Set
 - как работает хеш
- В чем особенность Map

# "Аджуманья"

Это не просто практика а еще и портфолио. Потом прикрепите к резюме:
- гитхаб
- leetcode
 
1. Любую движуху пишем на своем гитхабе. Будут копиться коммиты.
1. кто не писал код - JavaRush и аналоги (тренируем спинной мозг)
2. Кто писал и пишет код - Практика на LeetCode (все задачи на собесах по литкоду)
- Берем изи задачки на String и array