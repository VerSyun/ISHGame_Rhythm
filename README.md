# ISHGame_Rhythm
Игра, созданная в качестве курсовой работы.
Игра в жанре ритм, где игрок нажимает клавиши в такт музыки. Можно создавать собственные уровни.
## Что нужно для запуска
1. Для запуска нужен следующий софт:
- Android Studio с установленным SDK или IntelliJ
- Java 11
2. Открываем проект с игрой через build.gradle
3. Затем, чтобы запустить игру, идем в Run -> Edit configurations..., создаем Application
4. Выставляем настройки, как на скрине(папка Working directory определится сама):
![alt text](https://github.com/VerSyun/ISHGame_Rhythm/blob/main/setup.PNG)
5. Запускаем. Чтобы выбрать уровень заходим в *папка с игрой*\ISHGame_Rhythm\assets\Levels и выбираем файл в формате .key. Пока доступны только "Love" и "Muta Arcadia"
6. Играем.
7. Радуемся
## Как создавать свои уровни.
1. Выбираем музыку в формате .wav или .mp3
2. Закидываем ее в папку assets (путь к ней выше)
3. Создаем текстовый документ формата .key
4. В первой строчке пишем название музыку (вместе с форматом)
5. Во второй задаем длительность
6. Во всех последующих пишем пару, состоящую из клавиши (для справки A, S, D, F) и времени появления квардратика (в миллисекундах - 0.000)
   Совет - ко времени стоит прибавлять 200-300 миллисекунд, потому что воспроизведение начинается с задержкой, как фиксить пока не знаю.
7. Закидываем этот документ туда же.

## Используемый софт
- LibGDX 1.10.0
- Android Studio
- Gradle 7.4.2

Версия игры - 1.0a. Игра будет дорабатываться и будет гораздо удобней.