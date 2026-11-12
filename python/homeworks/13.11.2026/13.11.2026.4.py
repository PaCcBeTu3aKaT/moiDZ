summa = 0
minimum = None
maksimum = None

while True:
    chislo = float (input ("Введите число: "))

    if chislo == 7:
        break

    summa += chislo

    if minimum is None or chislo < minimum:
        minimum = chislo

    if maksimum is None or chislo > maksimum:
        maksimum = chislo

if minimum is None:
    print ("Числа не введены")
else:
    print ("Сумма:", summa)
    print ("Максимум:", maksimum)
    print ("Минимум:", minimum)

print ("Good bye!")
