import random

uroven = int (input ("Выберите уровень сложности:\n1. Легкий (5 вопросов, числа от 2 до 5)\n2. Средний (10 вопросов, числа от 2 до 9)\n3. Сложный (15 вопросов, числа от 10 до 20)\nВаш выбор (1-3): "))

if uroven == 1:
    voprosov = 5
    min_chislo = 2
    max_chislo = 5
elif uroven == 2:
    voprosov = 10
    min_chislo = 2
    max_chislo = 9
elif uroven == 3:
    voprosov = 15
    min_chislo = 10
    max_chislo = 20
else:
    print ("Некорректный уровень. Выбран средний уровень.")
    voprosov = 10
    min_chislo = 2
    max_chislo = 9

pravilno = 0

for i in range (1, voprosov + 1):
    a = random.randint (min_chislo, max_chislo)
    b = random.randint (min_chislo, max_chislo)
    pravilny_otvet = a * b

    print ("\nВопрос", i, "из", voprosov)
    print ("Сколько будет", a, "*", b, "?")
    otvet = int (input ("Ваш ответ: "))

    if otvet == pravilny_otvet:
        print ("Правильно!")
        pravilno += 1
    else:
        print ("Неправильно. Правильный ответ:", pravilny_otvet)

procent = (pravilno / voprosov) * 100

print ("\nРезультаты")
print ("Правильных ответов:", pravilno, "из", voprosov)
print ("Процент верных:", int (procent), "%")

if procent >= 90:
    ocenka = "Отлично (5)"
elif procent >= 75:
    ocenka = "Хорошо (4)"
elif procent >= 60:
    ocenka = "Удовлетворительно (3)"
else:
    ocenka = "Неудовлетворительно (2)"

print ("Ваша оценка:", ocenka)
