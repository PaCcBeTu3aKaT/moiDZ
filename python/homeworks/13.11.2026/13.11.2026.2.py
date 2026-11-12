dlina = int (input ("Длина линии: "))
simvol = input ("Символ: ")

if dlina < 0:
    print ("Длина не может быть отрицательной")
elif len (simvol) != 1:
    print ("Нужен ровно один символ")
else:
    for nomer in range (dlina):
        print (simvol)
