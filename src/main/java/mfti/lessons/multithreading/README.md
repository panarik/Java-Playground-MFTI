# Проект с примерами который разбираем
ссылка на директорию проекта: https://github.com/panarik/Java-Playground/tree/master/src/main/java/com/github/panarik/javaLesson/lessons/multithreading

# Домашнее задание
Текст ДЗ: [imports/ДЗ-многопоточка.md](imports/ДЗ-многопоточка.md)

Суть: написать **свой пул потоков** без `ExecutorService` / `ThreadPoolExecutor`. 5 задач, каждая строится на предыдущей:

| № | Задача | Баллы |
|---|--------|-------|
| 1 | Пул потоков: `execute`, `shutdownAfterAllTasks`, `shutdownNow`, `isClosed` | 1 |
| 2 | `Result`: `get`, `onSuccess`, `onFailure`, `isFinished` | 1 |
| 3 | Быстрые и медленные задачи (один быстрый поток + N медленных) | 2 |
| 4 | Тесты на пул, включая тесты на race condition | 1 |
| 5 | Профилирование: версия с блокировками против версии без них (бонус) | +2 |

Плюс собеседование (до 6 баллов).

---

# Примеры из проекта для выполнения ДЗ

Примеры по многопоточности лежат в папке `lessons/multithreading/` (ссылка выше). Часть материала берём из соседних пакетов того же проекта: `lang/collections`, `lang/exceptions`, `lang/generic`, `architecture/patterns`, `net/consoleserver`. Пути ниже указаны от папки `lessons/`.

Если для какого-то пункта ДЗ в проекте нет готового примера, это отмечено, и такой пункт разбираем отдельно.

## Задача 1. Пул потоков

### Создание потоков и ожидание их завершения
- Пример: `multithreading/threads/ThreadsCreate.java`.
- На семинаре смотрим способы создать поток (`extends Thread`, `Runnable`, лямбда) и задать ему имя через `setName`. Имя удобно при отладке воркеров.
- `join()` и `join(timeout)` нужны, чтобы в `shutdownAfterAllTasks` дождаться завершения воркеров.

### Потокобезопасность метода execute()
- Примеры: `multithreading/threads/ThreadsSync.java`, `multithreading/threads/ThreadsSyncMult.java`.
- В `ThreadsSync` три потока увеличивают общий счётчик. Запускаем пример без `synchronized` и с ним и сравниваем результат: без синхронизации часть инкрементов теряется. Так же будут теряться задачи, если добавлять их в очередь из нескольких потоков без защиты.
- `ThreadsSyncMult` показывает отдельные мониторы для независимых данных. Для пула достаточно одного монитора на очередь и состояние пула.

### Ожидание воркера и пробуждение: wait / notifyAll
- Пример: `multithreading/threads/ThreadsWait.java`. Ключевой пример для первой задачи.
- Воркер ждёт на мониторе, пока очередь пуста, а `execute()` будит его через `notifyAll()`.
- Обращаем внимание, что `wait()` стоит внутри `while (условие)`, а не внутри `if`: поток может проснуться без `notify` (ложное пробуждение). Это частый вопрос на собеседовании.

### Замок вместо synchronized: ReentrantLock
- Пример: `multithreading/Locks.java` (`simpleLock`, `tryLock`, `reentrantReadWriteLock`).
- Очередь и состояние пула можно защитить не через `synchronized`, а через `ReentrantLock`: `lock()` вместо входа в блок, `unlock()` в `finally`. В `simpleLock` два потока работают с одним замком: второй ждёт, пока первый его отпустит.
- `wait/notifyAll` работают только внутри `synchronized`. Если очередь защищена `ReentrantLock`, воркера усыпляем и будим через `Condition` (`await` / `signal`). В `Locks.java` этого нет, разбираем отдельно.
- `tryLock` пытается взять замок без ожидания и возвращает `false`, если он занят. В пуле нужен редко, но хорошо показывает разницу между блокирующим и неблокирующим захватом. К этому вернёмся в задаче 5 и на собеседовании.
- `ReentrantReadWriteLock`: читают параллельно несколько потоков, пишет один. Для очереди не подходит, потому что `execute()` и воркеры всегда её меняют. Подойдёт для данных, которые часто читают и редко меняют (например, состояние для `isClosed()`), хотя для одного флага проще `volatile`.
- Замок освобождаем только в `finally`, иначе после исключения он останется захваченным.

### Очередь задач
- Примеры: `lang/collections/ListExample.java` и `multithreading/threads/ThreadsWait.java`.
- Готовой очереди (`Queue`, `Deque`, `LinkedList`) в проекте нет. В качестве хранилища берём `ArrayList` из `ListExample`:
  - `add` в конец списка соответствует `execute()`;
  - `get(0)` и `remove(0)` берут самую старую задачу (FIFO);
  - `isEmpty()` и `size()` дают условие ожидания воркера в `while (...) wait()`;
  - `clear()` и `iterator()` с `iterator.remove()` используем для очистки очереди в `shutdownNow()`.
- В `ListExample` есть `add`, `get`, `size`, `contains`, `remove(Object)`, `clear`, `iterator.remove`. Удаления по индексу там нет, но `remove(0)` это тот же `List`.
- `ArrayList` не потокобезопасен, поэтому все обращения к нему делаем внутри `synchronized (monitor)`, как со счётчиком в `ThreadsSync`.
- `ArrayList.remove(0)` сдвигает элементы списка. Для учебной задачи это приемлемо. `ArrayDeque` и `LinkedList` сдвига не дают, но в проекте их нет.
- Готовые блокирующие очереди (`BlockingQueue`) на семинаре не используем: ожидание и пробуждение реализуем вручную через `wait/notifyAll`.

### Задача как объект
- Пример: `architecture/patterns/behavioral/command/` (`Command`, `ButtonRemoteController`, `TurnAllOffCommand`).
- Паттерн «Команда» устроен так же, как `Runnable`: `Command.execute()` соответствует `Runnable.run()`. `ButtonRemoteController` хранит команду и выполняет её позже, не зная, что внутри. Очередь пула так же хранит задачи. Отсюда понятно, почему `execute(Runnable task)` принимает интерфейс.
- `TurnAllOffCommand` это команда, которая выполняет список команд. К ней вернёмся в задаче 3.

### Остановка воркера: interrupt
- Примеры: `multithreading/threads/ThreadsInterrupt.java` (метод `daemon2()`), `net/consoleserver/Client.java`, `net/consoleserver/Server.java`.
- Поток работает в цикле `while (!Thread.currentThread().isInterrupted())`, а останавливается вызовом `interrupt()` из другого потока. На этом строятся воркер и `shutdownNow()`.
- Нюансы:
  - В `ThreadsInterrupt.daemon1()` `InterruptedException` ловится внутри цикла и игнорируется, поэтому `interrupt()` такой поток не остановит. Пример сделан для другой темы (потоки-демоны, см. ниже), но по этому признаку его можно разобрать как образец того, как делать не надо.
  - В `daemon2()` цикл завершается потому, что `try/catch` стоит вокруг цикла. После перехвата `InterruptedException` флаг прерывания сбрасывается. В воркере либо выходим из цикла, либо возвращаем флаг через `Thread.currentThread().interrupt()`. В примерах этого нет, объясняем отдельно.
  - В `net/consoleserver` цикл крутится вхолостую: когда ввода нет (`bufferedReader.ready()` возвращает `false`), в цикле нет ни паузы, ни ожидания, и процессор остаётся загруженным. В воркере пула вместо этого используем `wait()`.
  - `readLine()` блокирует поток и, как правило, не реагирует на `interrupt()`, поэтому в примере используется `ready()`. `wait()` и `sleep()` на `interrupt()` реагируют.

### Потоки-демоны
- Пример: `multithreading/threads/ThreadsInterrupt.java` (метод `daemon1()`).
- В `daemon1()` поток с бесконечным циклом помечен `setDaemon(true)`. Когда `main` завершается, программа закрывается вместе с ним, хотя цикл ещё работает. Обычный (не демон) поток с таким циклом удержал бы программу навсегда.
- Для пула это решение: если воркеры не демоны, программа не завершится, пока пул не закрыт через `shutdownAfterAllTasks()` или `shutdownNow()`. Если воркеры демоны, программа может закрыться раньше, чем выполнятся задачи из очереди. Выбираем вариант при разборе задачи 1, в ДЗ явно не указано.

### Исключение в задаче не должно останавливать воркер
- Пример: `lang/exceptions/Exceptions.java`.
- `try / catch / finally`: исключение перехватываем и продолжаем работу, `finally` выполняется всегда (освободить замок, уменьшить счётчик активных задач).
- `try/catch` ставим внутри цикла воркера, вокруг одного вызова `task.run()`. Если обернуть весь цикл, то после первого исключения поток выйдет из цикла и завершится. Такого примера в проекте нет, разбираем отдельно.

### Состояние пула и isClosed()
- Пример: `architecture/patterns/creational/singleton/Singleton.java`.
- Это единственный пример с `volatile` в проекте: `private static volatile Singleton instance`. Для `isClosed()` нужен такой же флаг (`volatile boolean closed`): его меняет `shutdown*()`, а читают `execute()` и воркеры.
- В `Singleton` `volatile` избыточен, так как `getInstance()` уже `synchronized`. Видимость изменений между потоками (Java Memory Model) разбираем отдельно: примера, где без `volatile` программа работает неверно, в проекте нет.

### Ориентир по поведению: ExecutorService
- Пример: `multithreading/ExecutorServiceExample.java`.
- В решении использовать нельзя, смотрим только как образец поведения. Пул из 4 потоков и 10 задач: одновременно выполняются не более 4. `shutdown()` соответствует `shutdownAfterAllTasks()`, `awaitTermination` ожиданию завершения. Закомментированный `shutdownNow()` возвращает список невыполненных задач.

## Задача 2. Result (аналог Future)

### Метод get() с ожиданием результата
- Примеры: `multithreading/Callables.java`, `multithreading/threads/ThreadsWait.java`.
- `FutureTask` и `Callable` в `Callables` это прообраз `Result`. `task.get()` блокирует вызывающий поток, `get(2, SECONDS)` выбрасывает `TimeoutException`, исключение из задачи приходит в `ExecutionException`.
- Ожидание в собственном `Result` пишем сами: `wait/notifyAll` на мониторе внутри `Result`, как в `ThreadsWait`. Воркер после записи результата вызывает `notifyAll()`.

### Состояния RUNNING, FINISHED, FAILED
- Примеры: `architecture/patterns/behavioral/state/` (`ATMState`, `ATM`, `HasCard`), `lang/enumExample/WeekDayNumber.java`.
- Паттерн «Состояние»: объект хранит текущее состояние и переключает его (`ATM.setState`). Недопустимое действие в данном состоянии ничего не меняет, например `HasCard.insertCard()` только печатает «Card already inserted.». Для `Result` то же правило: переход возможен только из `RUNNING`.
- Состояния удобно описать через `enum` (пример enum с полями: `WeekDayNumber`).

### Исключение из задачи
- Примеры: `lang/exceptions/Exceptions.java`, `lang/exceptions/MyException.java`, `multithreading/Callables.java`.
- Воркер перехватывает исключение через `try/catch`, сохраняет его в `Result` и переводит состояние в `FAILD`. `get()` выбрасывает это исключение вызывающему коду, обёрнутым, как `ExecutionException` в `Callables`. Собственный тип исключения: `MyException extends RuntimeException`.

## Задача 3. Быстрые и медленные задачи

Готовых примеров по этой задаче почти нет, поэтому большую часть разбираем отдельно.

### Ограничение времени на задачу
- Пример: `multithreading/Callables.java` (`get(2, SECONDS)` и `TimeoutException`).
- Показывает идею «задача уложилась во время или нет». Но по условию ДЗ задачу по таймауту просто прервать нельзя.

### Быстрый поток и несколько медленных
- Пример: `multithreading/ExecutorServiceExample.java` (идея нескольких воркеров).
- Две очереди и два вида воркеров это задача 1, применённая дважды. Если воркер и очередь вынесены в отдельные классы, их можно использовать повторно.

### Перенос задачи с одного потока на другой
- Примера в проекте нет, разбираем отдельно.
- Java не позволяет перенести выполнение метода с одного потока на другой в середине работы. Поэтому задачу разбиваем на короткие шаги, а состояние между шагами сохраняем в самой задаче. Быстрый воркер выполняет шаги, пока не вышел лимит времени, затем передаёт оставшуюся часть в очередь медленных.
- Для разбора подготовим простую задачу, например суммирование чисел от 1 до N по шагам.

### Задача как список шагов
- Примеры: `architecture/patterns/behavioral/command/commands/TurnAllOffCommand.java`, `architecture/patterns/behavioral/iterator/` (`SongsIterator`, `SongsArrayList`), `lang/collections/ListExample.java` (`iterateEx`).
- `TurnAllOffCommand` выполняет список команд по очереди, то есть шаги в цикле. `Iterator` (`iterator()`, `hasNext()`, `next()`) помнит позицию: можно выполнить несколько шагов, остановиться и позже продолжить с того же места.
- Вместе это даёт идею: задача = список шагов + итератор. Быстрый воркер выполняет шаги до истечения лимита, остаток вместе с итератором уходит в очередь медленных. Это направление для разбора, а не готовое решение.
- Итератор не потокобезопасен, поэтому передавать такую задачу между потоками нужно через очередь под замком.

### Отмена и прерывание
- Пример: `multithreading/threads/ThreadsInterrupt.java`.
- Объясняем, почему поток нельзя просто «остановить» (`Thread.stop()` запрещён) и можно только договориться через `interrupt`. Это вопрос на собеседовании.

## Задача 4. Тестирование

Готовых тестов в проекте нет. Примеры «ломающегося» кода используем как идеи для тестов на race condition и deadlock.

### Race condition
- Пример: `multithreading/threads/ThreadsSync.java`.
- Идея теста: N потоков вызывают `execute()` по M раз, затем сверяем число выполненных задач: должно получиться ровно N·M. Без синхронизации счётчик, как в примере, даёт меньше.
- Для воспроизводимости запускаем сценарий в цикле много раз, это допускается в ДЗ.

### Deadlock и зависание
- Пример: `multithreading/threads/ThreadsDeadLock.java`.
- Показывает, как программа зависает, когда потоки берут замки в разном порядке.
- Идея теста: проверка, что пул не зависает, то есть запуск с ограничением по времени (`@Test(timeout=...)` или `join(timeout)`).

### Параллельное выполнение
- Пример: `multithreading/ExecutorServiceExample.java`.
- В выводе видно имя потока и порядок «started / finished».
- Идея теста: счётчик одновременно работающих задач, его максимум не должен превышать размер пула.

### shutdown и shutdownNow
- Примеры: `multithreading/ExecutorServiceExample.java`, `multithreading/threads/ThreadsInterrupt.java`.
- Идея теста: после `shutdownNow` выполняющаяся задача получила `InterruptedException`, а задачи из очереди не запускались.

### Result и колбеки
- Пример: `multithreading/Callables.java`.
- Идея теста: проверить `get()` при успехе, при исключении и при таймауте.

### Нет потерь и повторов выполнения
- Примеры: `lang/collections/MapExample.java`, `lang/collections/SetExample.java`.
- Идея теста: даём каждой задаче номер и считаем запуски: `map.put(id, map.getOrDefault(id, 0) + 1)` (оба метода есть в `MapExample`). В конце у каждого номера должно быть значение 1, тогда повторов нет. `Set` не хранит дубликатов (`SetExample`), по его размеру видно, сколько разных задач выполнено, то есть нет ли потерь.
- `HashMap` и `HashSet` не потокобезопасны, а писать в них будут из разных потоков. Нужен либо `synchronized`, либо `ConcurrentHashMap` (его в проекте нет). Иначе гонка возникнет уже в самом тесте.

### JUnit
- JUnit подключён в `pom.xml`, пример теста: `src/test/java/mfti/lessons/FirstTest.java`. Версия в `pom.xml` указана как `RELEASE`, для стабильной сборки лучше указать конкретную (например, JUnit 5).

## Задача 5. Профилирование (бонус)

### Версия с блокировками
- Пример: `multithreading/Locks.java`.
- Блокирующий вариант пула строим на `ReentrantLock` (`simpleLock`) и `Condition`; `tryLock` и `ReentrantReadWriteLock` разобраны в задаче 1. В конце сравниваем его с вариантом без блокировок.

### Версия без блокировок
- Примера в проекте нет, разбираем отдельно.
- Понадобятся `AtomicInteger`, `AtomicReference`, `compareAndSet` (CAS), `ConcurrentLinkedQueue`.

### Измерения
- Примера в проекте нет, разбираем отдельно.
- Минимум: `System.nanoTime()` вокруг прогона одинаковой нагрузки. Для CPU и ожидания потоков используем VisualVM или `jcmd` / JFR.

---

# План семинара

Порядок разбора, от простого к сложному:

1. `multithreading/threads/ThreadsCreate.java`: создание потоков, `join`. 
ГОТОВО!

2. `multithreading/threads/ThreadsSync.java`: гонка данных и `synchronized` (сначала запускаем без `synchronized`, затем с ним).
ГОТОВО!

3. `multithreading/threads/ThreadsSyncMult.java`: разные мониторы.


4. `lang/collections/ListExample.java`: список как хранилище очереди задач.
Лучше дописать пример LinkedList


5. `multithreading/threads/ThreadsWait.java`: `wait` / `notifyAll`. На нём строятся очередь задач (задача 1) и `Result.get()` (задача 2).
ГОТОВО!

6. `multithreading/Locks.java`: `ReentrantLock` как замена `synchronized`, `tryLock`, `ReadWriteLock`. `Condition` как замена `wait/notify` разбираем отдельно.
ГОТОВО!

7. `multithreading/threads/ThreadsInterrupt.java` и `net/consoleserver/`: прерывание потока, `shutdownNow`, потоки-демоны.


8. `multithreading/threads/ThreadsDeadLock.java`: deadlock. Вопрос на собеседовании: возможен ли deadlock в вашем пуле.
ГОТОВО!

9. `multithreading/Callables.java`: `Callable` и `Future`, прообраз `Result`.


10. `architecture/patterns/behavioral/` (`command`, `observerPattern`, `state`, `iterator`), `lang/generic/`, `lang/exceptions/`: материал для `Result`, колбеков и задачи 3.


11. `multithreading/ExecutorServiceExample.java`: в конце, как образец поведения пула.