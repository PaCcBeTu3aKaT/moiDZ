storona = int (input ("Введите размер стороны квадрата: "))

if storona > 0:
    print ("*" * storona)
    for i in range (storona - 2):
        print ("*" + " " * (storona - 2) + "*")
    if storona > 1:
        print ("*" * storona)
