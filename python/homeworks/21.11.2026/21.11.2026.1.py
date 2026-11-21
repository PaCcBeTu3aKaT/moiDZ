stroka = input ("Введите строку: ")
ochishchennayaStroka = ""

for simvol in stroka.lower ():
    if simvol.isalnum ():
        ochishchennayaStroka += simvol

if ochishchennayaStroka == ochishchennayaStroka[::-1]:
    print ("Строка является палиндромом")
else:
    print ("Строка не является палиндромом")
