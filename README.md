# Платформа парсинга неструктурированнных данных
Асинхронный фреймворк для парсинга данных на языке Java.
# 
Система разбита на 3 модуля:
1. Producer — модуль который отвечает за способ получения данных.
2. Parser — модуль в котором описана логика парсинга и задается последовательность выполнения
3. Completer — модуль который отвечает за сохранение данных

   
![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/a671b92b-21c8-4be7-8ce9-d4431b1346d3)

## Producer
Данный модуль инкапсулирует в себе логику получения данных. Получение данных может быть совершенно разным, будь-то по HTTP запросу, из файла или от другой системы в составном проекте. Чтобы данный модуль был универсальным и не зависел от конкретных реализаций получения данных, он реализован с помощью шаблона адаптер.

Producer представляет собой интерфейс, включающий метод execute. Данный метод получает на вход объект типа ProducerIn и возвращает объект типа ProducerOut. ProducerIn является интерфейсом, предназначенным для хранения информации о местонахождении данных для последующего этапа, а интерфейс ProducerOut содержит данные, готовые для проведения парсинга. ProducerIn и ProducerOut служат абстракцией для связи между реализациями интерфейса Producer и модулем, отвечающим за парсинг данных.
### ApacheHttpClient
Реализация Producer по умолчанию будет основываться на библиотеке Apache Http Client.

## Parser
Parser является основным модулем, ответственным за управление последовательностью запросов, асинхронное поведение системы, конфигурацию и другие функции.
Метод start используется для инициации последовательности выполнения шагов парсинга. Метод next принимает шаг парсинга в качестве входных данных и выполняет его обработку. Метод join ожидает завершения всех асинхронных задач, прежде чем продолжить выполнение.
ParsingStep является классом, представляющим собой шаг парсинга, и содержит следующую информацию:
* Поле in типа ProducerIn, представляющее объект для получения данных.
* Поле authIn типа ProducerIn, представляющее объект для получения данных для аутентификации.
* Поле auth типа BiFunction<ProducerIn, ProducerOut, ProducerIn>, которое является бинарной функцией, описывающей логику аутентификации. Она принимает два объекта типа ProducerOut, полученные от объекта in типа ProducerIn, и объект типа ProducerIn без аутентификации, и возвращает объект типа ProducerIn с примененной аутентификацией.
* Поле handlers типа List<Consumer<ProducerOut>>, представляющее список обработчиков на данном шаге. Каждый обработчик представлен функциональным интерфейсом Consumer и содержит логику парсинга, определенную пользователем.
* Поле end типа Function<ProducerOut, ? extends Result>, которое является завершающей функцией. Она принимает объект типа ProducerOut и возвращает объект типа Result.
### AbstactParser
Абстрактный класс AbstractParser реализует интерфейс Parser и содержит два метода: next и join. Метод next выполняет обработку текущего шага парсинга и создание асинхронных задач. В целях избежания лишней синхронизации и использования общих ресурсов, этот метод выполняется одновременно в нескольких потоках и не изменяет состояние класса. Каждый поток обрабатывает свой экземпляр объекта типа Producer. Согласно требованиям, необходимо реализовать возможность выполнения запросов с интервалом. Для этого класс содержит блокирующую очередь объектов типа Producer, позволяющую каждому потоку получать свой экземпляр Producer из очереди и возвращать его в очередь с помощью новой асинхронной задачи. Эта задача засыпает на определенное время перед возвращением объекта в очередь. Для создания очереди необходимо предоставить абстрактную фабрику, которая создает объекты типа Producer.
Для выполнения асинхронных задач класс использует пул потоков, что позволяет переиспользовать потоки и избежать накладных расходов, связанных с созданием и уничтожением потоков при выполнении задач. Для создания асинхронных задач используется класс CompletableFuture. Каждый метод CompletableFuture, оканчивающийся на "Async", принимает пул потоков в качестве аргумента, на котором будет выполняться задача.
## Completer
Completer представляет собой функциональный интерфейс, который содержит один метод complete. Метод принимает на вход объект типа Result и содержит логику сохранения объекта Метод должен быть синхронизирован, так как будет вызываться из нескольких потоков одновременно.
### CSVCompleter
Реализация Completer по умолчанию будет реализовывать алгоритм сохранения объектов в csv файл. В конструкторе вызывается метод createNewFile, который создает файл и записывается заголовок, который содержит все объявленные поля итоговой модели. Метод complete получает на вход объект, который необходимо записать в файл и передает в метод saveNewRow. С помощью рефлексии метод получает значение каждого поля объекта и сохраняет в файл. Данный метод синхронизирован, что позволяет ему работать в многопоточной среде.

# Пример
Для решения данной задачи с помощью платформы парсинга необходимо написать класс, который будет наследован от AbstartParser.

![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/cefc61e0-b003-4eec-b74f-211d460e5c7c)

Далее необходимо реализовать методы start и get и составить цепочку шагов парсинга.

![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/a84a6bab-7934-4702-90d7-36b382162b3e)

Для создания цепочки заданий необходимо вызвать метод next и передать в качестве аргумента объект типа ParsingStep.

![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/87e0fdf9-5642-40a3-b7b2-e0179e8a6e30)

Объект Request содержит ссылку на главную страницу, а в качестве обработчика передана ссылка на метод для получения списка категорий. Метод getCategories занимается извлечением всех ссылок на товары на 1 странице. В качестве обработчика следующего шага выступают методы getProducts и nextPage. Метод nextPage уникален тем, что создает циклическую связь, выставляя в качестве обработчика следующего шага метод nextPage.

![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/640d5dc9-8af6-47bb-ae64-a11e46657085)

Последний метод getProduct отвечает за сбор ссылок на каждый товар и в качестве завершающего метода выступает get.

![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/4db0f43c-52ff-47db-b2b3-b657be0b58c5)

После создания класса, в котором описана логика парсинга необходимо запустить процесс парсинга. Для этого необходимо создать объект класса ProductMarketTest с параметрами интервал между запросами, списком прокси серверов и абстрактную фабрику для создания объектов типа Producer.

![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/e1746c6e-f75d-4a7d-baf4-2d1264843ece)


# Анализ

График зависимости скорости работы платформы от количества потоков

![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/ef683712-a16d-460e-a697-1fd9853d50e4)

Сравнение синхронного и асинхронного парсеров

![image](https://github.com/Elbundo/ParsingPlatform/assets/52011777/e7e37b88-5b33-449e-a1b8-90ce02e234a2)

Из данного графика видно, что асинхронный вариант парсера устойчивее к возрастанию количества запросов чем синхронный вариант

