nachalo = int (input ("Первое число: "))
konec = int (input ("Второе число: "))

if nachalo > konec:
    nachalo, konec = konec, nachalo

summa_chetnyh = 0
summa_nechetnyh = 0
summa_kratnyh_9 = 0
kolichestvo_chetnyh = 0
kolichestvo_nechetnyh = 0
kolichestvo_kratnyh_9 = 0

for chislo in range (nachalo, konec + 1):
    if chislo % 2 == 0:
        summa_chetnyh += chislo
        kolichestvo_chetnyh += 1
    else:
        summa_nechetnyh += chislo
        kolichestvo_nechetnyh += 1

    if chislo % 9 == 0:
        summa_kratnyh_9 += chislo
        kolichestvo_kratnyh_9 += 1

print ("Чётные: сумма =", summa_chetnyh)
print ("Чётные: среднее =", summa_chetnyh / kolichestvo_chetnyh if kolichestvo_chetnyh else "нет чисел")
print ("Нечётные: сумма =", summa_nechetnyh)
print ("Нечётные: среднее =", summa_nechetnyh / kolichestvo_nechetnyh if kolichestvo_nechetnyh else "нет чисел")
print ("Кратные 9: сумма =", summa_kratnyh_9)
print ("Кратные 9: среднее =", summa_kratnyh_9 / kolichestvo_kratnyh_9 if kolichestvo_kratnyh_9 else "нет чисел")
