dlina = int (input ("Введите длину (высоту) прямоугольника: "))
shirina = int (input ("Введите ширину прямоугольника: "))

if dlina > 0 and shirina > 0:
    if shirina == 1:
        for i in range (dlina):
            print ("*")
    else:
        print ("*" * shirina)
        for i in range (dlina - 2):
            print ("*" + " " * (shirina - 2) + "*")
        if dlina > 1:
            print ("*" * shirina)
