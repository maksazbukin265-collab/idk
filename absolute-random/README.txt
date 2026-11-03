Absolute Random — Fabric mod for Minecraft Java 26.3

Что делает:
- При ломании любого блока в выживании вместо обычного дропа выпадает случайный предмет из доступного реестра Minecraft.
- Командные/технические предметы исключены.
- В творческом режиме мод не вмешивается.

Сборка:
1. Установи JDK 25 (Minecraft 26.1+ требует Java 25).
2. Установи Gradle 9.6+.
3. Открой папку проекта и выполни:
   gradle build
4. Готовый JAR будет в build/libs/absolute-random-1.0.0.jar

Для запуска:
- Minecraft Java 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.165.2+26.3
- Скопируй JAR мода и Fabric API в .minecraft/mods.

Важно: мод предназначен для Fabric, не Forge/NeoForge.


Сборка через GitHub Actions:
1. Создай новый репозиторий GitHub.
2. Распакуй ZIP и загрузи ВСЕ файлы и папки из этой папки проекта в репозиторий.
3. Открой вкладку Actions. Workflow "Build Absolute Random mod" запустится после загрузки.
4. Когда сборка завершится, открой её и скачай Artifact "absolute-random-minecraft-26.3".
5. Внутри artifact будет JAR.
