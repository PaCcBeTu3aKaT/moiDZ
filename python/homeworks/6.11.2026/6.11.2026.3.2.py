chisla = [5, -3, 8, -6, 12, 7, 3, -9, -2, 11]

print ("Исходный список: ", chisla)

chetnye = []
nechetnye = []
otricatelnye = []
polozhitelnye = []

for i in range (len (chisla)):
    if chisla[i] % 2 == 0:
        chetnye.append (chisla[i])
    else:
        nechetnye.append (chisla[i])

    if chisla[i] < 0:
        otricatelnye.append (chisla[i])

    if chisla[i] > 0:
        polozhitelnye.append (chisla[i])

print ("Четные числа: ", chetnye)
print ("Нечетные числа: ", nechetnye)
print ("Отрицательные числа: ", otricatelnye)
print ("Положительные числа: ", polozhitelnye)
