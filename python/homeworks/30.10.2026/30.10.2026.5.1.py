chislo_vvod = int (input ("Введите целое число: "))
chislo_str = str (abs (chislo_vvod))

while True:
    vybor = input ("\nМЕНЮ\n1. Количество цифр\n2. Сумма цифр\n3. Среднее арифметическое цифр\n4. Количество нулей\n5. Ввести другое число\n0. Выход\nВыберите пункт: ")

    if vybor == "1":
        print ("Количество цифр:", len (chislo_str))

    elif vybor == "2":
        summa = 0
        for simvol in chislo_str:
            summa += int (simvol)
        print ("Сумма цифр:", summa)

    elif vybor == "3":
        summa = 0
        for simvol in chislo_str:
            summa += int (simvol)
        srednee = summa / len (chislo_str)
        print ("Среднее арифметическое цифр:", round (srednee, 2))

    elif vybor == "4":
        nuli = 0
        for simvol in chislo_str:
            if simvol == "0":
                nuli += 1
        print ("Количество нулей:", nuli)

    elif vybor == "5":
        chislo_vvod = int (input ("Введите новое целое число: "))
        chislo_str = str (abs (chislo_vvod))

    elif vybor == "0":
        print ("Программа завершена.")
        break

    else:
        print ("Неверный пункт меню.")
