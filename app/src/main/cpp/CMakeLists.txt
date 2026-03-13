cmake_minimum_required(VERSION 3.22.1)

project("samp-mobile")

# Добавляем библиотеку
add_library(
        samp-mobile
        SHARED
        main.cpp
)

# Находим стандартные библиотеки Android (логирование и т.д.)
find_library(
        log-lib
        log
)

target_link_libraries(
        samp-mobile
        ${log-lib}
)