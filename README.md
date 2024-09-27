# EventCountdown
Приложение представляет собой календарь с таймером обратного отсчета до важных событий. Поможет узнать день годовщины юбилея с красивым оформлением.
Предусмотрено оповещение при работе в фоне.

Разработал на основе технического задания и дизайн-макета в Figma:
[макет в Figma](https://www.figma.com/design/CICLcnLqz3YblWnd60his8/%D0%9E%D0%B1%D1%80%D0%B0%D1%82%D0%BD%D1%8B%D0%B9-%D0%BE%D1%82%D1%81%D1%87%D0%B5%D1%82?node-id=0-1).

Использованные в проекте технологии:
- Jetpack Compose;
- WorkManager;
- Coroutines;
- паттерн MVVM;
- Hilt;
- Navigation;
- Retrofit;
- Room;
- JSON.

Приложение сделано по заказу компании KiteCoding.

<img src="https://github.com/MaksimPronenko/EventCountdown/blob/main/assets/photo_1_2024-09-27_15-17-15.jpg" width="270" height="585">
<img src="https://github.com/MaksimPronenko/EventCountdown/blob/main/assets/photo_2_2024-09-27_15-17-15.jpg" width="270" height="585">
<img src="https://github.com/MaksimPronenko/EventCountdown/blob/main/assets/photo_5_2024-09-27_15-17-15.jpg" width="270" height="585">
<img src="https://github.com/MaksimPronenko/EventCountdown/blob/main/assets/photo_4_2024-09-27_15-17-15.jpg" width="270" height="585">
<img src="https://github.com/MaksimPronenko/EventCountdown/blob/main/assets/photo_3_2024-09-27_15-17-15.jpg" width="270" height="585">
<img src="https://github.com/MaksimPronenko/EventCountdown/blob/main/assets/photo_8_2024-09-27_15-17-15.jpg" width="270" height="585">
<img src="https://github.com/MaksimPronenko/EventCountdown/blob/main/assets/photo_6_2024-09-27_15-17-15.jpg" width="270" height="585">
<img src="https://github.com/MaksimPronenko/EventCountdown/blob/main/assets/photo_7_2024-09-27_15-17-15.jpg" width="270" height="585">

## Инструкция по запуску приложения

Приложение написано на Android Studio + gradle.

1. Открыть проект на GitHub, нажать кнопку "Code" и скопировать ссылку HTTPS.
   
2. Открыть папку, в которую будем клонировать приложение.
   
3. Открыть GitBash, набрать "git clone", вставить скопированную ссылку, нажимем "Enter", дождаться завершения процесса клонирования.
  
4. Запустить Android Studio. Выбрать "File", затем в выпавшем списке нажать "Open", выбрать клонированное приложение, в той папке, где его разместили. Нажать "OK". Приложение начнёт загружаться: внизу справа появится индикатор прогресса.

5. По мере загрузки справа снизу появятся сообщения с рекомендациями об обновлениях. Принять их и обновить.
   
6. Когда всё загрузилось, нажать Device Manager. Затем нажать "Create Virtual Device" (значок "+").
   
7. По умолчанию открывается вкладка "Phone". В ней выбрать устройство, которое нужноэмулировать. Например, средний по размеру "Pixel 5". Можно выбрать и другой телефон, приложение сделано для устройства любого размера.

8. Далее выбрать API Level. Например, API 34 "UpsideDownCake". В правой части окна могут появиться рекомендации. Например, может появиться сообщение "HAXM is not installed. Install HAXM" (речь идёт о механизме виртуализации для компьютеров на базе процессоров Intel). Установить. Если появляется окно с предложением выбрать объём оперативной памяти для эмулятора, выбрать рекомендованный. Другие рекомендации тоже нужно исполнить.

9. Если SDK Component для нужного уровня API не установлен, запустить его установку, нажав кнопку справа от имени API. По завершении утсановки нажать "Next".

10. В открывшемся окне выбрать Startap Orientation Portrait. Нажать "Finish".

11. В верхней части окна Andoio появится имя созданного виртуального устройства. Правее него располагается кнопка запуска приложения (зелёная стрелка вправо). Нажать её. В правой нижней части окна Android Studio появится индикация прогресса загрузки. Первая загрузка приложения на новый эмулятор может занять несколько минут. По завершении приложение откроется на эмуляторе.
