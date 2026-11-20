try:
    pervoeChislo = int (input ("Введите первое целое число: "))
    vtoroeChislo = int (input ("Введите второе целое число: "))
    rezultat = pervoeChislo / vtoroeChislo
    print (rezultat)
except ValueError:
    print ("Нужно ввести два целых числа")
except ZeroDivisionError:
    print ("На ноль делить нельзя")
