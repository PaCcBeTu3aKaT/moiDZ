while True:
    chislo = float (input ("Введите число: "))

    if chislo == 7:
        print ("Good bye!")
        break
    elif chislo > 0:
        print ("Number is positive")
    elif chislo < 0:
        print ("Number is negative")
    else:
        print ("Number is equal to zero")
