"""nachalo = int (input ("Введите начало диапазона: "))
konec = int (input ("Введите конец диапазона: "))

sum_chet = 0
sum_nechet = 0
sum_9 = 0

kol_chet = 0
kol_nechet = 0
kol_9 = 0

for i in range(nachalo, konec + 1):
    if i % 2 == 0:
        sum_chet += i
        kol_chet += 1
    else:
        sum_nechet += i
        kol_nechet += 1

    if i % 9 == 0:
        sum_9 += i
        kol_9 += 1

print ("Сумма чётных чисел:", sum_chet)
if kol_chet > 0:
    print ("Среднее чётных чисел:", sum_chet / kol_chet)
else:
    print ("Чётных чисел нет")

print ("Сумма нечётных чисел:", sum_nechet)
if kol_nechet > 0:
    print ("Среднее нечётных чисел:", sum_nechet / kol_nechet)
else:
    print ("Нечётных чисел нет")

print ("Сумма чисел, кратных 9:", sum_9)
if kol_9 > 0:
    print ("Среднее чисел, кратных 9:", sum_9 / kol_9)
else:
    print ("Чисел, кратных 9, нет")"""


"""dlina = int (input ("Введите длину линии: "))
simvol = input ("Введите символ: ")

for i in range (dlina):
    print (simvol)"""


"""while True:
    chislo = int (input ("Введите число: "))

    if chislo == 7:
        print ("Good bye!")
        break
    elif chislo > 0:
        print ("Number is positive")
    elif chislo < 0:
        print ("Number is negative")
    else:
        print ("Number is equal to zero")"""


summa = 0
maksimum = None
minimum = None

while True:
    chislo = int (input ("Введите число: "))

    if chislo == 7:
        print ("Good bye!")
        break

    summa += chislo

    if maksimum is None or chislo > maksimum:
        maksimum = chislo

    if minimum is None or chislo < minimum:
        minimum = chislo

if maksimum is not None:
    print("Сумма:", summa)
    print("Максимум:", maksimum)
    print("Минимум:", minimum)
else:
    print("Числа не были введены")