#!/usr/bin/env bash
# Скачивает официальный Forge MDK, накладывает на него исходники мода и собирает jar.
set -euxo pipefail

FV="${FORGE_VERSION:-1.21.11-61.0.3}"
URL="https://maven.minecraftforge.net/net/minecraftforge/forge/${FV}/forge-${FV}-mdk.zip"

rm -rf mdk mdk_inner out && mkdir mdk
curl -fL "$URL" -o mdk.zip
unzip -q mdk.zip -d mdk

# Если MDK лежит во вложенной папке — поднимаем её наверх
if [ ! -f mdk/gradlew ]; then
  sub=$(find mdk -maxdepth 3 -name gradlew -printf '%h\n' | head -1)
  mv "$sub" mdk_inner
  rm -rf mdk
  mv mdk_inner mdk
fi

echo "=== MDK: список файлов ==="
(cd mdk && find . -maxdepth 4 -not -path './gradle/*' -not -path './.git/*' | sort)
echo "=== MDK: gradle.properties ==="
cat mdk/gradle.properties
echo "=== MDK: build.gradle ==="
cat mdk/build.gradle

# Убираем пример мода, кладём свой код
rm -rf mdk/src/main/java
mkdir -p mdk/src/main/java
cp -r src/. mdk/src/

# Настройки мода в gradle.properties
sed -i 's/^mod_id=.*/mod_id=visuals/' mdk/gradle.properties
sed -i 's/^mod_group_id=.*/mod_group_id=com.example/' mdk/gradle.properties
sed -i 's/^mod_name=.*/mod_name=Visuals/' mdk/gradle.properties

# mods.toml: если в MDK он генерируется из шаблона — подменяем шаблон нашим файлом
if [ -d mdk/src/main/templates ]; then
  mkdir -p mdk/src/main/templates/META-INF
  cp src/main/resources/META-INF/mods.toml mdk/src/main/templates/META-INF/mods.toml
  rm -f mdk/src/main/resources/META-INF/mods.toml
fi

cd mdk
chmod +x gradlew
./gradlew build --no-daemon --stacktrace

echo "=== Результат ==="
ls -la build/libs
mkdir -p ../out
cp build/libs/*.jar ../out/
rm -f ../out/*-sources.jar ../out/*-javadoc.jar
ls -la ../out
