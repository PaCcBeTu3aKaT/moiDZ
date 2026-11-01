chisla = [5, -3, 8, -6, 12, 7, 3, -9, -2, 11]

print ("Список: ", chisla)

sum_otr = 0
sum_chet = 0
sum_nechet = 0
proizv_3 = 1

for i in range (len (chisla)):
    if chisla[i] < 0:
        sum_otr += chisla[i]

    if chisla[i] % 2 == 0:
        sum_chet += chisla[i]
    else:
        sum_nechet += chisla[i]

    if i % 3 == 0:
        proizv_3 *= chisla[i]

print ("Сумма отрицательных чисел: ", sum_otr)
print ("Сумма четных чисел: ", sum_chet)
print ("Сумма нечетных чисел: ", sum_nechet)
print ("Произведение элементов с индексами кратными 3: ", proizv_3)

min_i = chisla.index (min (chisla))
max_i = chisla.index (max (chisla))

nachalo = min (min_i, max_i) + 1
konec = max (min_i, max_i)

proizv = 1

for i in range (nachalo, konec):
    proizv *= chisla[i]

print ("Произведение между минимальным и максимальным: ", proizv)

perv = -1
posl = -1

for i in range (len (chisla)):
    if chisla[i] > 0:
        if perv == -1:
            perv = i
        posl = i

sum_pol = 0

if perv != -1 and perv != posl:
    for i in range (perv + 1, posl):
        sum_pol += chisla[i]

print ("Сумма между первым и последним положительными: ", sum_pol)
